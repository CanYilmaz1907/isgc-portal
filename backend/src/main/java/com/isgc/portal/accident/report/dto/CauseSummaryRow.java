package com.isgc.portal.accident.report.dto;

public record CauseSummaryRow(
    String category,
    long totalCount,
    String topSubCauseCode,
    String topSubCauseLabel,
    long topSubCauseCount
) {}
