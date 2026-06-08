package com.isgc.portal.dashboard;

import com.isgc.portal.accident.AccidentService;
import com.isgc.portal.accident.report.AccidentReportingService;
import com.isgc.portal.accident.dto.AccidentResponse;
import com.isgc.portal.accident.report.dto.AccidentDistributionResponse;
import com.isgc.portal.accident.report.dto.AccidentSeriesPoint;
import com.isgc.portal.discipline.dto.DisciplineResponse;
import com.isgc.portal.ncr.dto.NcrResponse;
import com.isgc.portal.nonconformity.dto.NonconformityResponse;
import com.isgc.portal.audit.AuditService;
import com.isgc.portal.discipline.DisciplineService;
import com.isgc.portal.ncr.NcrQueries;
import com.isgc.portal.ncr.NcrStatus;
import com.isgc.portal.nonconformity.NonconformityService;
import com.isgc.portal.security.CurrentUser;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final AccidentService accidentService;
  private final AccidentReportingService accidentReportingService;
  private final NonconformityService nonconformityService;
  private final DisciplineService disciplineService;
  private final AuditService auditService;
  private final NcrQueries ncrQueries;

  public DashboardController(
      AccidentService accidentService,
      AccidentReportingService accidentReportingService,
      NonconformityService nonconformityService,
      DisciplineService disciplineService,
      AuditService auditService,
      NcrQueries ncrQueries
  ) {
    this.accidentService = accidentService;
    this.accidentReportingService = accidentReportingService;
    this.nonconformityService = nonconformityService;
    this.disciplineService = disciplineService;
    this.auditService = auditService;
    this.ncrQueries = ncrQueries;
  }

  @GetMapping("/stats")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public DashboardStatsResponse stats(@AuthenticationPrincipal @NotNull CurrentUser user) {
    Instant now = Instant.now();
    Instant startOfMonth = now.minus(now.getEpochSecond() % 86400, ChronoUnit.SECONDS)
        .atZone(java.time.ZoneId.systemDefault())
        .withDayOfMonth(1)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .toInstant();

    long accidentsThisMonth = accidentService.list(user).stream()
        .filter(a -> a.occurredAt() != null && a.occurredAt().isAfter(startOfMonth))
        .count();

    long openNonconformities = nonconformityService.list(user).stream()
        .filter(n -> "OPEN".equals(n.status()))
        .count();

    long completedAudits = auditService.list(user).stream()
        .filter(a -> "COMPLETED".equals(a.status()))
        .count();

    long openDisciplineLogs = disciplineService.list(user).stream()
        .filter(d -> d.status() != null && !"SOZLESME_FESHI".equals(d.status().name()))
        .count();

    List<NcrResponse> ncrList = ncrQueries.list(user);
    long openNcr = ncrList.stream()
        .filter(n -> n.status() != NcrStatus.CLOSED)
        .count();
    long overdueNcr = ncrList.stream()
        .filter(n -> n.overdue())
        .count();

    return new DashboardStatsResponse(
        accidentsThisMonth,
        openNonconformities,
        openNcr,
        completedAudits,
        openDisciplineLogs,
        overdueNcr
    );
  }

  @GetMapping("/recent-accidents")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<AccidentResponse> recentAccidents(
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return accidentService.list(user).stream()
        .sorted((a, b) -> {
          if (a.occurredAt() == null && b.occurredAt() == null) return 0;
          if (a.occurredAt() == null) return 1;
          if (b.occurredAt() == null) return -1;
          return b.occurredAt().compareTo(a.occurredAt());
        })
        .limit(5)
        .toList();
  }

  @GetMapping("/recent-nonconformities")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<NonconformityResponse> recentNonconformities(
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return nonconformityService.list(user).stream()
        .sorted((a, b) -> {
          if (a.dueDate() == null && b.dueDate() == null) return 0;
          if (a.dueDate() == null) return 1;
          if (b.dueDate() == null) return -1;
          return b.dueDate().compareTo(a.dueDate());
        })
        .limit(5)
        .toList();
  }

  @GetMapping("/recent-ncr")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<NcrResponse> recentNcr(
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return ncrQueries.list(user).stream()
        .sorted((a, b) -> b.ncrDate().compareTo(a.ncrDate()))
        .limit(5)
        .toList();
  }

  @GetMapping("/recent-discipline-logs")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<DisciplineResponse> recentDisciplineLogs(
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return disciplineService.list(user).stream()
        .sorted((a, b) -> {
          if (a.occurredAt() == null && b.occurredAt() == null) return 0;
          if (a.occurredAt() == null) return 1;
          if (b.occurredAt() == null) return -1;
          return b.occurredAt().compareTo(a.occurredAt());
        })
        .limit(5)
        .toList();
  }

  @GetMapping("/accident-trend")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<AccidentSeriesPoint> accidentTrend(@AuthenticationPrincipal @NotNull CurrentUser user) {
    Instant now = Instant.now();
    Instant sixMonthsAgo = ZonedDateTime.ofInstant(now, java.time.ZoneId.systemDefault())
        .minusMonths(6)
        .toInstant();
    return accidentReportingService.series(sixMonthsAgo, now, null, null, null, "MONTH");
  }

  @GetMapping("/accident-distribution")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public AccidentDistributionResponse accidentDistribution(@AuthenticationPrincipal @NotNull CurrentUser user) {
    Instant now = Instant.now();
    Instant sixMonthsAgo = ZonedDateTime.ofInstant(now, java.time.ZoneId.systemDefault())
        .minusMonths(6)
        .toInstant();
    return accidentReportingService.distribution(sixMonthsAgo, now, null, null, null);
  }
}

