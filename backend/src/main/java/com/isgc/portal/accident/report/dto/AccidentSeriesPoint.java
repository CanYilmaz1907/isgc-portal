package com.isgc.portal.accident.report.dto;

import java.time.Instant;

public record AccidentSeriesPoint(Instant bucketStart, long count) {}


