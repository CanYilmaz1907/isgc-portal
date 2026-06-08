package com.isgc.portal.accident.dto;

import java.util.UUID;

public record AccidentTypeResponse(UUID id, String code, String name, String formSchemaJson, boolean enabled) {}


