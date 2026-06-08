package com.isgc.portal.me;

import java.util.UUID;

public record EmployeeSummary(
    UUID id,
    String employeeNo,
    String firstName,
    String lastName,
    String jobTitle,
    String profession,
    UUID projectId
) {}


