package com.isgc.portal.document;

import com.isgc.portal.document.dto.DocumentResponse;
import com.isgc.portal.files.FileObject;
import com.isgc.portal.security.CurrentUser;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
  private final DocumentService service;

  public DocumentController(DocumentService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public List<DocumentResponse> list() {
    return service.list();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public DocumentResponse get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public DocumentResponse create(
      @AuthenticationPrincipal @NotNull CurrentUser user,
      @RequestParam String code,
      @RequestParam String title,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String note,
      @RequestParam("file") MultipartFile file
  ) {
    return service.create(user, code, title, description, file, note);
  }

  @PostMapping("/{id}/versions")
  @PreAuthorize("hasRole('ADMIN')")
  public DocumentResponse newVersion(
      @PathVariable UUID id,
      @AuthenticationPrincipal @NotNull CurrentUser user,
      @RequestParam(required = false) String note,
      @RequestParam("file") MultipartFile file
  ) {
    return service.uploadNewVersion(user, id, file, note);
  }

  @GetMapping("/versions/{versionId}/download")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public ResponseEntity<Resource> download(@PathVariable UUID versionId) throws Exception {
    FileObject fo = service.requireVersionFile(versionId);
    Path p = Path.of(fo.getStoragePath()).toAbsolutePath().normalize();
    if (!Files.exists(p)) throw new IllegalArgumentException("File missing on disk");
    Resource res = new FileSystemResource(p);
    String contentType = fo.getContentType() != null ? fo.getContentType() : MediaType.APPLICATION_PDF_VALUE;
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fo.getOriginalFilename().replace("\"", "") + "\"")
        .contentType(MediaType.parseMediaType(contentType))
        .contentLength(fo.getSizeBytes())
        .body(res);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    service.delete(user, id);
  }
}


