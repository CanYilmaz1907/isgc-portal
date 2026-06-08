package com.isgc.portal.overview.dto;

import com.isgc.portal.audit.dto.AuditAnalysisResponse;
import java.math.BigDecimal;
import java.util.List;

public record AuditCompareResponse(
    String leftId,
    String leftTitle,
    String rightId,
    String rightTitle,
    AuditAnalysisResponse left,
    AuditAnalysisResponse right,
    List<CategoryDelta> categoryDeltas
) {
  public record CategoryDelta(
      int categoryNo,
      String label,
      BigDecimal leftPercent,
      BigDecimal rightPercent,
      BigDecimal delta
  ) {}
}
