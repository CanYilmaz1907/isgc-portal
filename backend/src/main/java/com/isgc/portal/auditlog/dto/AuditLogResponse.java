package com.isgc.portal.auditlog.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
    UUID id,
    UUID actorUserId,
    String actorUsername,
    String action,
    String entityType,
    UUID entityId,
    Map<String, Object> details,
    Instant createdAt
) {}

