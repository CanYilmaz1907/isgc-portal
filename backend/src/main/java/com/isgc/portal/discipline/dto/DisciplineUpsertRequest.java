package com.isgc.portal.discipline.dto;

import com.isgc.portal.discipline.DisciplineCategory;
import com.isgc.portal.discipline.DisciplineStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DisciplineUpsertRequest(
    UUID projectId,
    @NotNull Instant occurredAt,
    String fullName,
    String employeeRegistrationNo,
    @NotBlank String company,
    @NotBlank String jobTitle,
    @NotBlank String workArea,
    @NotNull DisciplineCategory categoryLevel,
    @NotBlank String violationType,
    @NotBlank String violationDescription,
    @NotBlank String responsiblePerson,
    @NotNull DisciplineStatus status,
    String notes,
    BigDecimal penaltyAmount,
    String profession,
    UUID violatingEmployeeId,
    UUID violatingManagerEmployeeId
) {}
