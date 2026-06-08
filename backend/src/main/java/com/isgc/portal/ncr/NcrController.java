package com.isgc.portal.ncr;

import com.isgc.portal.ncr.dto.NcrResponse;
import com.isgc.portal.ncr.dto.NcrUpsertRequest;
import com.isgc.portal.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ncr")
public class NcrController {
  private final NcrService ncrService;
  private final NcrPdfService ncrPdfService;

  public NcrController(NcrService ncrService, NcrPdfService ncrPdfService) {
    this.ncrService = ncrService;
    this.ncrPdfService = ncrPdfService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public List<NcrResponse> list(@AuthenticationPrincipal @NotNull CurrentUser user) {
    return ncrService.list(user);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public NcrResponse get(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return ncrService.get(user, id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public NcrResponse create(@Valid @RequestBody NcrUpsertRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return ncrService.create(user, req);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public NcrResponse update(@PathVariable("id") UUID id, @Valid @RequestBody NcrUpsertRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return ncrService.update(user, id, req);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    ncrService.delete(user, id);
  }

  @GetMapping("/{id}/pdf")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public ResponseEntity<byte[]> generatePdf(
      @PathVariable("id") UUID id,
      @RequestParam(value = "lang", defaultValue = "tr") String lang,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) throws IOException {
    NcrResponse response = ncrService.get(user, id);
    Ncr entity = ncrService.getEntity(id);
    byte[] pdfBytes = ncrPdfService.generatePdf(entity, response, lang);
    String filename = "ncr-" + entity.getNcrNumber().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(pdfBytes.length)
        .body(pdfBytes);
  }
}
