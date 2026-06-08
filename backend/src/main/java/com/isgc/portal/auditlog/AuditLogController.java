package com.isgc.portal.auditlog;

import com.isgc.portal.auditlog.dto.AuditLogResponse;
import com.isgc.portal.security.CurrentUser;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
  private final AuditLogService auditLogService;

  public AuditLogController(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public Page<AuditLogResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    return auditLogService.list(pageable);
  }

  @GetMapping("/entity/{entityType}/{entityId}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public List<AuditLogResponse> findByEntity(
      @PathVariable String entityType,
      @PathVariable UUID entityId,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return auditLogService.findByEntity(entityType, entityId);
  }

  @GetMapping("/date-range")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public List<AuditLogResponse> findByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return auditLogService.findByDateRange(from, to);
  }

  @GetMapping("/actor/{userId}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public List<AuditLogResponse> findByActor(
      @PathVariable UUID userId,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return auditLogService.findByActor(userId);
  }
}

