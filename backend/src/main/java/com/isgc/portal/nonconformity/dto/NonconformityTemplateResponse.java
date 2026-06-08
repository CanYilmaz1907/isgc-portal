package com.isgc.portal.nonconformity.dto;

import java.util.UUID;

public record NonconformityTemplateResponse(
    UUID id,
    String code,
    String name,
    String tableSchemaJson,
    boolean enabled
) {}


