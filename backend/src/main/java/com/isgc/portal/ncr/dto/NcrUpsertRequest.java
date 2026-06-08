package com.isgc.portal.ncr.dto;

import com.isgc.portal.ncr.NcrStatus;
import com.isgc.portal.ncr.NcrVerificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record NcrUpsertRequest(
    LocalDate ncrDate,
    UUID projectId,
    @NotBlank String responsibleOrganization,
    String location,
    String title,
    @NotBlank String description,
    String evidenceReferences,
    String proposedCorrectiveAction,
    String executedCorrectiveAction,
    LocalDate targetCompletionDate,
    LocalDate completionDate,
    List<String> rootCauseCategories,
    @NotNull NcrStatus status,
    String initiatedBy,
    String approvedBy,
    String verifiedBy,
    NcrVerificationStatus verificationStatus,
    List<String> isoStandards,
    boolean followupRequired,
    String notes,
    UUID responsibleEmployeeId,
    String classification,
    String rootCause,
    String preventiveAction
) {}
