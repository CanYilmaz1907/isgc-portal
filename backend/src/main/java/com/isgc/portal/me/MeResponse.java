package com.isgc.portal.me;

import java.util.UUID;

public record MeResponse(UUID userId, String username, String role) {}


