package com.isgc.portal.nonconformity.dto;

import com.isgc.portal.nonconformity.NonconformityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record NonconformityUpsertRequest(
    UUID projectId,
    @NotNull UUID templateId,
    UUID hazardClassId,
    UUID responsibleEmployeeId,
    @NotBlank String title,
    String description,
    LocalDate dueDate,
    NonconformityStatus status,
    String severity,
    String dataJson
) {}


