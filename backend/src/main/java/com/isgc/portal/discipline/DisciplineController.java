package com.isgc.portal.discipline;

import com.isgc.portal.discipline.dto.DisciplineResponse;
import com.isgc.portal.discipline.dto.DisciplineUpsertRequest;
import com.isgc.portal.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.isgc.portal.discipline.dto.DisciplineImportResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/discipline-logs")
public class DisciplineController {
  private static final Logger log = LoggerFactory.getLogger(DisciplineController.class);
  private final DisciplineService service;
  private final DisciplinePdfService pdfService;
  private final DisciplineLogRepository repo;
  private final DisciplineImportService importService;
  private final DisciplineExportService exportService;

  public DisciplineController(
      DisciplineService service,
      DisciplinePdfService pdfService,
      DisciplineLogRepository repo,
      DisciplineImportService importService,
      DisciplineExportService exportService
  ) {
    this.service = service;
    this.pdfService = pdfService;
    this.repo = repo;
    this.importService = importService;
    this.exportService = exportService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<DisciplineResponse> list(@AuthenticationPrincipal @NotNull CurrentUser user) {
    return service.list(user);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public DisciplineResponse get(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return service.get(user, id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public DisciplineResponse create(@Valid @RequestBody DisciplineUpsertRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return service.create(user, req);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public DisciplineResponse update(@PathVariable("id") UUID id, @Valid @RequestBody DisciplineUpsertRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return service.update(user, id, req);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    service.delete(user, id);
  }

  @PostMapping("/import")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public DisciplineImportResult importExcel(
      @RequestPart("file") MultipartFile file,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) throws Exception {
    return importService.importExcel(user, file);
  }

  @GetMapping("/export")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public ResponseEntity<byte[]> exportExcel(@AuthenticationPrincipal @NotNull CurrentUser user) throws Exception {
    byte[] data = exportService.exportExcel(user);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"disiplin-logu.xlsx\"")
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .contentLength(data.length)
        .body(data);
  }

  @GetMapping("/{id}/pdf")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public ResponseEntity<byte[]> generatePdf(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    try {
      log.info("Generating PDF for discipline log: {}", id);
      
      // Get entity with relationships loaded (includes access check)
      DisciplineLog disciplineLog = service.getEntityWithEmployees(user, id);
      log.info("Entity loaded with employees for discipline log: {}", id);

      log.info("Calling PDF service for discipline log: {}", id);
      byte[] pdfBytes = pdfService.generatePdf(disciplineLog);
      log.info("PDF generated successfully, size: {} bytes", pdfBytes.length);
      
      String filename = "disiplin-logu-" + id.toString().substring(0, 8) + ".pdf";

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .contentType(MediaType.APPLICATION_PDF)
          .contentLength(pdfBytes.length)
          .body(pdfBytes);
    } catch (IllegalArgumentException e) {
      log.error("Illegal argument exception while generating PDF for discipline log: {}", id, e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error while generating PDF for discipline log: {}", id, e);
      throw new IllegalStateException("PDF generation failed: " + e.getMessage(), e);
    }
  }
}


