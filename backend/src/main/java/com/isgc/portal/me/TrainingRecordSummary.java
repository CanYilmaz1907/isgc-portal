package com.isgc.portal.me;

import java.time.LocalDate;
import java.util.UUID;

public record TrainingRecordSummary(
    UUID id,
    String trainingName,
    String provider,
    LocalDate completedOn,
    LocalDate validUntil
) {}


