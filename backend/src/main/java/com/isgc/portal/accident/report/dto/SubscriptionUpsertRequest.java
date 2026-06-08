package com.isgc.portal.accident.report.dto;

import com.isgc.portal.accident.report.ReportFrequency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SubscriptionUpsertRequest(
    UUID projectId,
    boolean enabled,
    @NotNull ReportFrequency frequency,
    @Min(0) @Max(23) int hourOfDay,
    @Min(0) @Max(59) int minuteOfHour,
    @NotBlank String toEmails,
    String ccEmails,
    String filtersJson
) {}


