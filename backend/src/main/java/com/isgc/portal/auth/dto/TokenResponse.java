package com.isgc.portal.auth.dto;

import com.isgc.portal.user.Role;
import java.time.Instant;
import java.util.UUID;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    Instant accessTokenExpiresAt,
    UUID userId,
    String username,
    Role role
) {}


