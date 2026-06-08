package com.isgc.portal.accident.report.dto;

import com.isgc.portal.accident.report.ReportFrequency;
import java.util.UUID;

public record SubscriptionResponse(
    UUID id,
    UUID projectId,
    boolean enabled,
    ReportFrequency frequency,
    int hourOfDay,
    int minuteOfHour,
    String toEmails,
    String ccEmails,
    String filtersJson
) {}


