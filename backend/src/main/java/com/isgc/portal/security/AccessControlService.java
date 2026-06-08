package com.isgc.portal.security;

import com.isgc.portal.employee.Employee;
import com.isgc.portal.employee.EmployeeRepository;
import com.isgc.portal.user.Role;
import com.isgc.portal.user.RoleCapabilities;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {
  private final EmployeeRepository employeeRepository;

  public AccessControlService(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  public UUID requireEmployeeIdForUser(UUID userId) {
    return employeeRepository.findByUserId(userId)
        .map(Employee::getId)
        .orElseThrow(() -> new IllegalArgumentException("Employee record not found for user"));
  }

  public boolean canAccessEmployee(CurrentUser user, UUID employeeId) {
    if (RoleCapabilities.canViewAll(user.role())) {
      return true;
    }
    UUID myEmployeeId = employeeRepository.findByUserId(user.id())
        .map(Employee::getId)
        .orElse(null);
    if (myEmployeeId == null) {
      return false;
    }
    if (employeeId.equals(myEmployeeId)) {
      return true;
    }
    if (user.role() == Role.YONETICI) {
      Set<UUID> visible = new HashSet<>();
      for (Employee e : employeeRepository.findDirectReports(myEmployeeId)) {
        visible.add(e.getId());
      }
      return visible.contains(employeeId);
    }
    return false;
  }
}


