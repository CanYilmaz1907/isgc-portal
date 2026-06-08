package com.isgc.portal.audit.dto;

import com.isgc.portal.audit.AuditStatus;
import com.isgc.portal.audit.AuditType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuditResponse(
    UUID id,
    UUID projectId,
    String projectName,
    AuditType auditType,
    UUID checklistId,
    String checklistTitle,
    String title,
    String summary,
    AuditStatus status,
    Instant startedAt,
    Instant finishedAt,
    BigDecimal calculatedScore,
    String reportHtml,
    List<Participant> participants
) {
  public record Participant(UUID employeeId, String employeeName, String role) {}
}


