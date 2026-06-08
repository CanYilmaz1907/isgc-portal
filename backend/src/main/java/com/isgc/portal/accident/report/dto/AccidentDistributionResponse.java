package com.isgc.portal.accident.report.dto;

import java.util.Map;

public record AccidentDistributionResponse(
    Map<String, Long> byAccidentClass,
    Map<String, Long> byPotentialLevel
) {}


