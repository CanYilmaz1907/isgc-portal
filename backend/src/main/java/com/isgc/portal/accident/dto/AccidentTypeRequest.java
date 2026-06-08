package com.isgc.portal.accident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccidentTypeRequest(
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 255) String name,
    @NotBlank String formSchemaJson,
    boolean enabled
) {}


