package com.isgc.portal.audit;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/audits")
public class AuditFileController {
  public static final String FILE_MODULE = "AUDIT";

  private final AuditRepository auditRepo;
  private final AuditService auditService;
  private final FileObjectRepository fileRepo;
  private final FileStorageService storage;
  private final UserRepository userRepo;

  public AuditFileController(
      AuditRepository auditRepo,
      AuditService auditService,
      FileObjectRepository fileRepo,
      FileStorageService storage,
      UserRepository userRepo
  ) {
    this.auditRepo = auditRepo;
    this.auditService = auditService;
    this.fileRepo = fileRepo;
    this.storage = storage;
    this.userRepo = userRepo;
  }

  @GetMapping("/{auditId}/files")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public List<FileObjectResponse> list(@PathVariable("auditId") UUID auditId, @AuthenticationPrincipal @NotNull CurrentUser user) {
    auditService.get(user, auditId);
    return fileRepo.findByModuleAndEntityId(FILE_MODULE, auditId).stream().map(this::toDto).toList();
  }

  @PostMapping("/{auditId}/files")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public FileObjectResponse upload(
      @PathVariable("auditId") UUID auditId,
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) {
      throw new IllegalArgumentException("Access denied");
    }
    auditRepo.findById(auditId).orElseThrow(() -> new IllegalArgumentException("Audit not found"));
    if (file.isEmpty()) throw new IllegalArgumentException("Empty file");
    if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
      throw new IllegalArgumentException("Filename required");
    }

    UUID fileId = UUID.randomUUID();
    var stored = storage.store(FILE_MODULE, auditId, fileId, safeInputStream(file));
    FileObject fo = new FileObject();
    fo.setId(fileId);
    fo.setModule(FILE_MODULE);
    fo.setEntityId(auditId);
    fo.setOriginalFilename(file.getOriginalFilename());
    fo.setContentType(file.getContentType());
    fo.setSizeBytes(stored.sizeBytes());
    fo.setStoragePath(stored.storagePath());
    fo.setSha256(stored.sha256());
    fo.setUploadedBy(userRepo.findById(user.id()).orElse(null));
    fileRepo.save(fo);
    return toDto(fo);
  }

  @GetMapping("/{auditId}/files/{fileId}/download")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public ResponseEntity<Resource> download(
      @PathVariable("auditId") UUID auditId,
      @PathVariable("fileId") UUID fileId,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) throws Exception {
    auditService.get(user, auditId);
    FileObject fo = fileRepo.findById(fileId).orElseThrow(() -> new IllegalArgumentException("File not found"));
    if (!FILE_MODULE.equals(fo.getModule()) || !auditId.equals(fo.getEntityId())) {
      throw new IllegalArgumentException("File mismatch");
    }
    Path p = Path.of(fo.getStoragePath()).toAbsolutePath().normalize();
    if (!Files.exists(p)) throw new IllegalArgumentException("File missing on disk");
    Resource res = new FileSystemResource(p);
    String contentType = fo.getContentType() != null ? fo.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fo.getOriginalFilename().replace("\"", "") + "\"")
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
}


