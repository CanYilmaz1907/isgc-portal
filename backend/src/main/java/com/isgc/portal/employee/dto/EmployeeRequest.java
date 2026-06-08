package com.isgc.portal.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record EmployeeRequest(
    UUID projectId,
    @Size(max = 50) String employeeNo,
    @NotBlank @Size(max = 120) String firstName,
    @NotBlank @Size(max = 120) String lastName,
    @Size(max = 120) String jobTitle,
    @Size(max = 150) String profession,
    UUID primaryManagerEmployeeId,
    UUID userId,
    boolean enabled
) {}


