package com.isgc.portal.user.dto;

import com.isgc.portal.user.Role;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String email,
    Role role,
    boolean enabled,
    Instant createdAt,
    Instant updatedAt
) {}

