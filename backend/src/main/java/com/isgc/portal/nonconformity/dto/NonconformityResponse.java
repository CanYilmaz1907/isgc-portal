package com.isgc.portal.nonconformity.dto;

import com.isgc.portal.nonconformity.NonconformityStatus;
import java.time.LocalDate;
import java.util.UUID;

public record NonconformityResponse(
    UUID id,
    UUID projectId,
    String projectName,
    UUID templateId,
    String templateName,
    UUID hazardClassId,
    String hazardClassName,
    UUID responsibleEmployeeId,
    String responsibleEmployeeName,
    String title,
    String description,
    LocalDate dueDate,
    NonconformityStatus status,
    String severity,
    String dataJson
) {}


