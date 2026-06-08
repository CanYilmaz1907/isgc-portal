package com.isgc.portal.audit.dto;

import java.math.BigDecimal;
import java.util.List;

public record AuditAnalysisResponse(
    BigDecimal overallScore,
    String overallColorZone,
    List<AuditCategoryCompliance> categories,
    List<AuditItemCompliance> items
) {
  public record AuditItemCompliance(
      int itemNo,
      int categoryNo,
      String question,
      BigDecimal compliancePercent,
      String colorZone,
      boolean applicable,
      BigDecimal score,
      BigDecimal maxScore
  ) {}
}
