package com.isgc.portal.audit.dto;

import com.isgc.portal.audit.AuditType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record AuditCreateRequest(
    UUID projectId,
    @NotNull AuditType auditType,
    UUID checklistId,
    @NotBlank String title,
    String summary,
    List<Participant> participants
) {
  public record Participant(UUID employeeId, String role) {}
}


