package com.isgc.portal.audit.dto;

import java.util.UUID;

public record ChecklistResponse(
    UUID id,
    String code,
    String title,
    String scope,
    boolean enabled
) {}


