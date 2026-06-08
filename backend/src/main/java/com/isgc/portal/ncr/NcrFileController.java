package com.isgc.portal.ncr;

import com.isgc.portal.files.FileObject;
import com.isgc.portal.files.FileObjectRepository;
import com.isgc.portal.files.FileStorageService;
import com.isgc.portal.files.dto.FileObjectResponse;
import com.isgc.portal.security.CurrentUser;
import com.isgc.portal.user.Role;
import com.isgc.portal.user.UserRepository;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ncr")
public class NcrFileController {
  private final NcrRepository ncrRepo;
  private final NcrService ncrService;
  private final FileObjectRepository fileRepo;
  private final FileStorageService storage;
  private final UserRepository userRepo;

  public NcrFileController(
      NcrRepository ncrRepo,
      NcrService ncrService,
      FileObjectRepository fileRepo,
      FileStorageService storage,
      UserRepository userRepo
  ) {
    this.ncrRepo = ncrRepo;
    this.ncrService = ncrService;
    this.fileRepo = fileRepo;
    this.storage = storage;
    this.userRepo = userRepo;
  }

  @GetMapping("/{id}/files")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public List<FileObjectResponse> list(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    ncrService.get(user, id);
    return fileRepo.findByModuleAndEntityId(NcrService.FILE_MODULE, id).stream().map(this::toDto).toList();
  }

  @PostMapping("/{id}/files")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public FileObjectResponse upload(
      @PathVariable("id") UUID id,
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) {
      throw new IllegalArgumentException("Access denied");
    }
    ncrRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("NCR not found"));
    if (file.isEmpty()) throw new IllegalArgumentException("Empty file");
    if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
      throw new IllegalArgumentException("Filename required");
    }

    UUID fileId = UUID.randomUUID();
    var stored = storage.store(NcrService.FILE_MODULE, id, fileId, safeInputStream(file));

    FileObject fo = new FileObject();
    fo.setId(fileId);
    fo.setModule(NcrService.FILE_MODULE);
    fo.setEntityId(id);
    fo.setOriginalFilename(file.getOriginalFilename());
    fo.setContentType(file.getContentType());
    fo.setSizeBytes(stored.sizeBytes());
    fo.setStoragePath(stored.storagePath());
    fo.setSha256(stored.sha256());
    fo.setUploadedBy(userRepo.findById(user.id()).orElse(null));
    fileRepo.save(fo);
    return toDto(fo);
  }

  @PatchMapping("/{id}/files/{fileId}/display-name")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public FileObjectResponse updateDisplayName(
      @PathVariable("id") UUID id,
      @PathVariable("fileId") UUID fileId,
      @RequestBody UpdateDisplayNameRequest body,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    ncrService.get(user, id);
    FileObject fo = fileRepo.findById(fileId).orElseThrow(() -> new IllegalArgumentException("File not found"));
    if (!NcrService.FILE_MODULE.equals(fo.getModule()) || !id.equals(fo.getEntityId())) {
      throw new IllegalArgumentException("File mismatch");
    }
    fo.setDisplayName(body.displayName() != null && body.displayName().length() > 255
        ? body.displayName().substring(0, 255) : body.displayName());
    fileRepo.save(fo);
    return toDto(fo);
  }

  @GetMapping("/{id}/files/{fileId}/download")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public ResponseEntity<Resource> download(
      @PathVariable("id") UUID id,
      @PathVariable("fileId") UUID fileId,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) throws Exception {
    ncrService.get(user, id);
    FileObject fo = fileRepo.findById(fileId).orElseThrow(() -> new IllegalArgumentException("File not found"));
    if (!NcrService.FILE_MODULE.equals(fo.getModule()) || !id.equals(fo.getEntityId())) {
      throw new IllegalArgumentException("File mismatch");
    }
    Path p = Path.of(fo.getStoragePath()).toAbsolutePath().normalize();
    if (!Files.exists(p)) throw new IllegalArgumentException("File missing on disk");
    Resource res = new FileSystemResource(p);
    String contentType = fo.getContentType() != null ? fo.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    String filename = fo.getDisplayName() != null && !fo.getDisplayName().isBlank()
        ? fo.getDisplayName() : fo.getOriginalFilename();
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename.replace("\"", "") + "\"")
        .contentType(MediaType.parseMediaType(contentType))
        .contentLength(fo.getSizeBytes())
        .body(res);
  }

  private static java.io.InputStream safeInputStream(MultipartFile file) {
    try {
      return file.getInputStream();
    } catch (Exception e) {
      throw new IllegalStateException("Cannot read upload", e);
    }
  }

  private FileObjectResponse toDto(FileObject f) {
    return new FileObjectResponse(
        f.getId(),
        f.getModule(),
        f.getEntityId(),
        f.getOriginalFilename(),
        f.getDisplayName(),
        f.getContentType(),
        f.getSizeBytes(),
        f.getSha256(),
        f.getCreatedAt()
    );
  }

  public record UpdateDisplayNameRequest(String displayName) {}
}
