package com.isgc.portal.accident.dto;

import com.isgc.portal.accident.AccidentClass;
import com.isgc.portal.accident.AccidentStatus;
import com.isgc.portal.accident.PotentialLevel;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccidentUpsertRequest(
    UUID projectId,
    @NotNull UUID accidentTypeId,
    Instant occurredAt,
    String location,
    @NotNull AccidentClass accidentClass,
    @NotNull PotentialLevel potentialLevel,
    String description,
    String formDataJson,
    String rootCauseDataJson,
    List<UUID> injuredEmployeeIds,
    List<UUID> keyPersonEmployeeIds,
    AccidentStatus status,
    // Excel-based fields
    String area,
    String hazardSource,
    String injuredBodyPart,
    String injuryType,
    String employeeRegistrationNo,
    UUID supervisorEmployeeId,
    String timePeriod,
    // New report template fields
    String groupCompanyName,
    String responsiblePerson,
    String estimatedCost,
    Boolean workRelated,
    String workDuringAccident,
    Integer injuredPersonAge,
    String injuredPersonProfession,
    String injuredPersonGender,
    String injuredPersonNationality,
    String injuredPersonCompany,
    String actionsTakenJson,
    UUID preparedByUserId,
    Instant preparedAt,
    String classification,
    String personName,
    String durationOnProject,
    String durationInRole,
    String workSupervisor,
    Boolean emergencyNotificationSent,
    String vehiclePlate,
    String vehicleType,
    List<CauseSelectionDto> directCauses,
    List<CauseSelectionDto> rootCauses
) {}


