package com.isgc.portal.dashboard;

public record DashboardStatsResponse(
    long accidentsThisMonth,
    long openNonconformities,
    long openNcr,
    long completedAudits,
    long openDisciplineLogs,
    long overdueNcr
) {}

