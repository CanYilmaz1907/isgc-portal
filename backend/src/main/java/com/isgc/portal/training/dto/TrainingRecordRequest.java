package com.isgc.portal.training.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public record TrainingRecordRequest(
    UUID employeeId,
    @NotBlank String trainingName,
    String provider,
    LocalDate completedOn,
    LocalDate validUntil
) {}


