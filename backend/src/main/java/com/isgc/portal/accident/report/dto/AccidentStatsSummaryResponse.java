package com.isgc.portal.accident.report.dto;

public record AccidentStatsSummaryResponse(
    long total,
    long lti,
    long fat,
    long nearMiss
) {}

