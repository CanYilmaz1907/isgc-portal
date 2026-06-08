package com.isgc.portal.accident.report.dto;

import java.util.List;
import java.util.Map;

public record AccidentDashboardResponse(
    AccidentKpiResponse kpis,
    List<AccidentMonthlyStackPoint> monthlyTrend,
    Map<String, Long> classificationDistribution,
    List<LabelCountPoint> hazardSourceDistribution,
    List<CauseGroupCountPoint> directCauseGroups,
    List<CauseGroupCountPoint> rootCauseGroups,
    List<LabelCountPoint> bodyPartDistribution,
    Map<String, Long> timeRangeDistribution,
    Map<String, Long> areaDistribution,
    List<CauseSummaryRow> directCauseSummary,
    List<CauseSummaryRow> rootCauseSummary
) {}
