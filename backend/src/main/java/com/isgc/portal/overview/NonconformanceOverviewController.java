package com.isgc.portal.overview;

import com.isgc.portal.overview.dto.AuditCompareResponse;
import com.isgc.portal.overview.dto.NonconformanceOverviewResponse;
import com.isgc.portal.security.CurrentUser;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nonconformance-overview")
public class NonconformanceOverviewController {
  private final NonconformanceOverviewService service;

  public NonconformanceOverviewController(NonconformanceOverviewService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public NonconformanceOverviewResponse overview(@AuthenticationPrincipal @NotNull CurrentUser user) {
    return service.overview(user);
  }

  @GetMapping("/audit-compare")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public AuditCompareResponse compare(
      @RequestParam UUID leftId,
      @RequestParam UUID rightId,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return service.compare(user, leftId, rightId);
  }
}
