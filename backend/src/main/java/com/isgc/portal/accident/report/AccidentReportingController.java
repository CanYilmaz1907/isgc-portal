package com.isgc.portal.accident.report;

import com.isgc.portal.accident.report.dto.AccidentDistributionResponse;
import com.isgc.portal.accident.report.dto.AccidentSeriesPoint;
import com.isgc.portal.accident.report.dto.AccidentStatsSummaryResponse;
import com.isgc.portal.accident.report.dto.AccidentRootCausePoint;
import com.isgc.portal.security.CurrentUser;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accidents/stats")
public class AccidentReportingController {
  private final AccidentReportingService reporting;
  private final AccidentStatsExportService exportService;

  public AccidentReportingController(
      AccidentReportingService reporting,
      AccidentStatsExportService exportService
  ) {
    this.reporting = reporting;
    this.exportService = exportService;
  }

  @GetMapping("/series")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public List<AccidentSeriesPoint> series(
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(name = "projectId", required = false) UUID projectId,
      @RequestParam(name = "area", required = false) String area,
      @RequestParam(name = "accidentTypeId", required = false) UUID accidentTypeId,
      @RequestParam(name = "bucket", defaultValue = "MONTH") String bucket,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return reporting.series(from, to, projectId, area, accidentTypeId, bucket);
  }

  @GetMapping("/distribution")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public AccidentDistributionResponse distribution(
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(name = "projectId", required = false) UUID projectId,
      @RequestParam(name = "area", required = false) String area,
      @RequestParam(name = "accidentTypeId", required = false) UUID accidentTypeId,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return reporting.distribution(from, to, projectId, area, accidentTypeId);
  }

  @GetMapping("/summary")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public AccidentStatsSummaryResponse summary(
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(name = "projectId", required = false) UUID projectId,
      @RequestParam(name = "area", required = false) String area,
      @RequestParam(name = "accidentTypeId", required = false) UUID accidentTypeId,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return reporting.summary(from, to, projectId, area, accidentTypeId);
  }

  @GetMapping("/root-causes")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public List<AccidentRootCausePoint> rootCauses(
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(name = "projectId", required = false) UUID projectId,
      @RequestParam(name = "area", required = false) String area,
      @RequestParam(name = "accidentTypeId", required = false) UUID accidentTypeId,
      @RequestParam(name = "limit", defaultValue = "10") int limit,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return reporting.rootCauses(from, to, projectId, area, accidentTypeId, limit);
  }

  @GetMapping("/dashboard")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public com.isgc.portal.accident.report.dto.AccidentDashboardResponse dashboard(
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(name = "projectId", required = false) UUID projectId,
      @RequestParam(name = "area", required = false) String area,
      @RequestParam(name = "accidentTypeId", required = false) UUID accidentTypeId,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    return reporting.dashboard(from, to, projectId, area, accidentTypeId);
  }

  @GetMapping("/export")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public ResponseEntity<byte[]> exportExcel(
      @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(name = "projectId", required = false) UUID projectId,
      @RequestParam(name = "area", required = false) String area,
      @RequestParam(name = "accidentTypeId", required = false) UUID accidentTypeId,
      @RequestParam(name = "bucket", defaultValue = "MONTH") String bucket,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) throws Exception {
    byte[] data = exportService.exportExcel(from, to, projectId, area, accidentTypeId, bucket);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"kaza-raporu.xlsx\"")
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(data);
  }
}


