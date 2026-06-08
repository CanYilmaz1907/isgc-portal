package com.isgc.portal.accident.dto;

import com.isgc.portal.accident.AccidentClass;
import com.isgc.portal.accident.PotentialLevel;
import java.util.UUID;

public record NotificationRuleResponse(
    UUID id,
    AccidentClass accidentClass,
    PotentialLevel potentialLevel,
    String toEmails,
    String ccEmails,
    boolean enabled
) {}


