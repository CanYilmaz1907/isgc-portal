package com.isgc.portal.discipline.dto;

import java.util.Map;

public record DisciplineSummaryStatsResponse(
    long totalWarnings,
    long totalPenalties,
    Map<String, Long> byCategory,
    Map<String, Long> byMonth,
    Map<String, Long> byResponsiblePerson,
    Map<String, Long> byCompany
) {}
