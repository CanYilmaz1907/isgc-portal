package com.isgc.portal.nonconformity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NonconformityTemplateRequest(
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 255) String name,
    @NotBlank String tableSchemaJson,
    boolean enabled
) {}


