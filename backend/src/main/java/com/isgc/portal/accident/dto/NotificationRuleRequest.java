package com.isgc.portal.accident.dto;

import com.isgc.portal.accident.AccidentClass;
import com.isgc.portal.accident.PotentialLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRuleRequest(
    @NotNull AccidentClass accidentClass,
    @NotNull PotentialLevel potentialLevel,
    @NotBlank String toEmails,
    String ccEmails,
    boolean enabled
) {}


