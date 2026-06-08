package com.isgc.portal.audit;

import com.isgc.portal.audit.dto.AuditAnalysisResponse;
import com.isgc.portal.audit.dto.AuditCreateRequest;
import com.isgc.portal.audit.dto.AuditItemResultResponse;
import com.isgc.portal.audit.dto.AuditResponse;
import com.isgc.portal.audit.dto.AuditSubmitRequest;
import com.isgc.portal.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audits")
public class AuditController {
  private final AuditService auditService;
  private final AuditAnalysisService analysisService;

  public AuditController(AuditService auditService, AuditAnalysisService analysisService) {
    this.auditService = auditService;
    this.analysisService = analysisService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public List<AuditResponse> list(@AuthenticationPrincipal @NotNull CurrentUser user) {
    return auditService.list(user);
  }

  @GetMapping("/archive")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public List<AuditResponse> archive(@AuthenticationPrincipal @NotNull CurrentUser user) {
    return auditService.list(user).stream()
        .filter(a -> a.status() == AuditStatus.COMPLETED)
        .toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public AuditResponse get(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return auditService.get(user, id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public AuditResponse create(@Valid @RequestBody AuditCreateRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return auditService.create(user, req);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public AuditResponse update(@PathVariable("id") UUID id, @Valid @RequestBody AuditCreateRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return auditService.update(user, id, req);
  }

  @PostMapping("/{id}/start")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public AuditResponse start(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return auditService.start(user, id);
  }

  @PostMapping("/{id}/complete")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public AuditResponse complete(@PathVariable("id") UUID id, @Valid @RequestBody AuditSubmitRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return auditService.submitAndComplete(user, id, req);
  }

  @GetMapping("/{id}/results")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public List<AuditItemResultResponse> results(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return analysisService.results(user, id);
  }

  @GetMapping("/{id}/analysis")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public AuditAnalysisResponse analysis(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return analysisService.analysis(user, id);
  }
}


