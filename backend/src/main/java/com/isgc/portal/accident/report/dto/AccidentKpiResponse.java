package com.isgc.portal.accident.report.dto;

public record AccidentKpiResponse(
    long total,
    long fat,
    long lti,
    long mtc,
    long fac,
    long nearMiss,
    long equipment
) {}
