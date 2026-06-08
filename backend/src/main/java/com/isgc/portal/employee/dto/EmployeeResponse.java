package com.isgc.portal.employee.dto;

import java.util.UUID;

public record EmployeeResponse(
    UUID id,
    UUID projectId,
    String employeeNo,
    String firstName,
    String lastName,
    String jobTitle,
    String profession,
    UUID primaryManagerEmployeeId,
    UUID userId,
    boolean enabled
) {}


