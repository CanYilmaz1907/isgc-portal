package com.isgc.portal.audit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ChecklistItemUpsertRequest(
    @NotNull UUID checklistId,
    int itemNo,
    Integer categoryNo,
    @NotBlank String question,
    @NotNull @DecimalMin("0.01") BigDecimal weight,
    @NotNull @DecimalMin("0.00") BigDecimal maxScore,
    boolean enabled
) {}


