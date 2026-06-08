package com.isgc.portal.accident.dto;

import com.isgc.portal.accident.AccidentClass;
import com.isgc.portal.accident.AccidentStatus;
import com.isgc.portal.accident.PotentialLevel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccidentResponse(
    UUID id,
    UUID projectId,
    String projectName,
    UUID accidentTypeId,
    String accidentTypeName,
    Instant occurredAt,
    String location,
    AccidentClass accidentClass,
    PotentialLevel potentialLevel,
    String description,
    String formDataJson,
    String rootCauseDataJson,
    AccidentStatus status,
    List<PersonRef> injured,
    List<PersonRef> keyPeople,
    // Excel-based fields
    String area,
    String hazardSource,
    String injuredBodyPart,
    String injuryType,
    String employeeRegistrationNo,
    PersonRef supervisorEmployee,
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
    PersonRef preparedBy,
    Instant preparedAt,
    Integer incidentNo,
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
) {
  public record PersonRef(UUID employeeId, String firstName, String lastName) {}
}


