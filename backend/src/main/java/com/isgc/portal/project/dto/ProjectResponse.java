package com.isgc.portal.project.dto;

import java.util.UUID;

public record ProjectResponse(
    UUID id,
    String code,
    String name,
    boolean enabled
) {}


