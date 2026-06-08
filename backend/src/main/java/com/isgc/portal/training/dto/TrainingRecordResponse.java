package com.isgc.portal.training.dto;

import java.time.LocalDate;
import java.util.UUID;

public record TrainingRecordResponse(
    UUID id,
    UUID employeeId,
    String trainingName,
    String provider,
    LocalDate completedOn,
    LocalDate validUntil
) {}


