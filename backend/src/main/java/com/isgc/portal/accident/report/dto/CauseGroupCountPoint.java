package com.isgc.portal.accident.report.dto;

public record CauseGroupCountPoint(
    String groupCode,
    String groupName,
    String section,
    long count
) {}
