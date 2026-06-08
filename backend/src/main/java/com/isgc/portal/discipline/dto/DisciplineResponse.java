package com.isgc.portal.discipline.dto;

import com.isgc.portal.discipline.DisciplineCategory;
import com.isgc.portal.discipline.DisciplineStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DisciplineResponse(
    UUID id,
    Integer sequenceNo,
    UUID projectId,
    Instant occurredAt,
    String fullName,
    String employeeRegistrationNo,
    String company,
    String jobTitle,
    String workArea,
    DisciplineCategory categoryLevel,
    String violationType,
    String violationTypeLabel,
    String violationDescription,
    String responsiblePerson,
    DisciplineStatus status,
    String notes,
    int repeatCount,
    boolean repeatThresholdReached,
    BigDecimal penaltyAmount,
    int severity,
    String profession,
    UUID violatingEmployeeId,
    String violatingEmployeeName,
    UUID violatingManagerEmployeeId,
    String violatingManagerEmployeeName
) {}
