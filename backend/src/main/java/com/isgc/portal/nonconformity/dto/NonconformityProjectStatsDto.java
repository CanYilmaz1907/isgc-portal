package com.isgc.portal.nonconformity.dto;

import java.util.Map;
import java.util.UUID;

public record NonconformityProjectStatsDto(
    UUID projectId,
    String projectName,
    Map<String, Long> byHazardClass
) {}
