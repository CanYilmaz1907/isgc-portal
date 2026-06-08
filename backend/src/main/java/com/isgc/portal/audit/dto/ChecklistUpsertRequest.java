package com.isgc.portal.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChecklistUpsertRequest(
    @NotBlank @Size(max = 80) String code,
    @NotBlank @Size(max = 255) String title,
    String scope,
    boolean enabled
) {}


