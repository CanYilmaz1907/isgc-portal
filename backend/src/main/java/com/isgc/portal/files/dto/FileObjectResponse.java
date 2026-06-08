package com.isgc.portal.files.dto;

import java.time.Instant;
import java.util.UUID;

public record FileObjectResponse(
    UUID id,
    String module,
    UUID entityId,
    String originalFilename,
    String displayName,
    String contentType,
    long sizeBytes,
    String sha256,
    Instant createdAt
) {}


