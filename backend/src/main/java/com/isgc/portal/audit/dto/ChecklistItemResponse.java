package com.isgc.portal.audit.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ChecklistItemResponse(
    UUID id,
    UUID checklistId,
    int itemNo,
    int categoryNo,
    String question,
    BigDecimal weight,
    BigDecimal maxScore,
    boolean enabled
) {}


