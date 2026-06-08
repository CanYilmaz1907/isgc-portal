package com.isgc.portal.overview.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record NonconformanceOverviewResponse(
    Map<String, Long> moduleTotals,
    Map<String, Long> openByModule,
    long overdueNcr,
    BigDecimal avgAuditCompliance,
    Map<String, Long> ncrByStatus,
    Map<String, Map<String, Long>> monthlyTrend,
    List<AuditScorePoint> recentAuditScores
) {
  public record AuditScorePoint(String id, String title, String projectName, BigDecimal score, String finishedAt) {}
}
