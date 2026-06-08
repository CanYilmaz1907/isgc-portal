package com.isgc.portal.accident.report.dto;

import java.util.Map;

public record AccidentMonthlyStackPoint(
    String month,
    Map<String, Long> byClassification
) {}
