package com.isgc.portal.employee;

import com.isgc.portal.employee.dto.EmployeeRequest;
import com.isgc.portal.employee.dto.EmployeeResponse;
import com.isgc.portal.project.ProjectRepository;
import com.isgc.portal.security.AccessControlService;
import com.isgc.portal.security.CurrentUser;
import com.isgc.portal.user.Role;
import com.isgc.portal.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
  private final EmployeeRepository employeeRepository;
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final AccessControlService accessControlService;

  public EmployeeController(
      EmployeeRepository employeeRepository,
      ProjectRepository projectRepository,
      UserRepository userRepository,
      AccessControlService accessControlService
  ) {
    this.employeeRepository = employeeRepository;
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.accessControlService = accessControlService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<EmployeeResponse> list(@AuthenticationPrincipal @NotNull CurrentUser user) {
    if (user.role() == Role.YONETICI) {
      UUID managerEmployeeId = accessControlService.requireEmployeeIdForUser(user.id());
      List<Employee> direct = new ArrayList<>(employeeRepository.findDirectReports(managerEmployeeId));
      direct.add(employeeRepository.findById(managerEmployeeId).orElseThrow());
      return direct.stream().map(this::toResponse).toList();
    }
    if (user.role() == Role.PERSONEL) {
      // PERSONEL can only see themselves
      UUID employeeId = accessControlService.requireEmployeeIdForUser(user.id());
      Employee e = employeeRepository.findById(employeeId).orElseThrow();
      return List.of(toResponse(e));
    }
    // ADMIN and ISG_C can see all enabled employees
    return employeeRepository.findAll().stream()
        .filter(Employee::isEnabled)
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public EmployeeResponse get(@PathVariable UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    if (!accessControlService.canAccessEmployee(user, id)) {
      throw new IllegalArgumentException("Access denied");
    }
    return employeeRepository.findById(id).map(this::toResponse)
        .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
  }

  @GetMapping("/me")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public EmployeeResponse me(@AuthenticationPrincipal @NotNull CurrentUser user) {
    Employee e = employeeRepository.findByUserId(user.id())
        .orElseThrow(() -> new IllegalArgumentException("Employee record not found for user"));
    return toResponse(e);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public EmployeeResponse create(@Valid @RequestBody EmployeeRequest req) {
    Employee e = new Employee();
    e.setId(UUID.randomUUID());
    apply(req, e);
    employeeRepository.save(e);
    return toResponse(e);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public EmployeeResponse update(@PathVariable UUID id, @Valid @RequestBody EmployeeRequest req) {
    Employee e = employeeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    apply(req, e);
    employeeRepository.save(e);
    return toResponse(e);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable UUID id) {
    employeeRepository.deleteById(id);
  }

  private void apply(EmployeeRequest req, Employee e) {
    if (req.projectId() != null) {
      e.setProject(projectRepository.findById(req.projectId())
          .orElseThrow(() -> new IllegalArgumentException("Project not found")));
    } else {
      e.setProject(null);
    }
    e.setEmployeeNo(req.employeeNo());
    e.setFirstName(req.firstName());
    e.setLastName(req.lastName());
    e.setJobTitle(req.jobTitle());
    e.setProfession(req.profession());
    if (req.primaryManagerEmployeeId() != null) {
      e.setPrimaryManager(employeeRepository.findById(req.primaryManagerEmployeeId())
          .orElseThrow(() -> new IllegalArgumentException("Primary manager not found")));
    } else {
      e.setPrimaryManager(null);
    }
    if (req.userId() != null) {
      e.setUser(userRepository.findById(req.userId()).orElseThrow(() -> new IllegalArgumentException("User not found")));
    } else {
      e.setUser(null);
    }
    e.setEnabled(req.enabled());
  }

  private EmployeeResponse toResponse(Employee e) {
    return new EmployeeResponse(
        e.getId(),
        e.getProject() != null ? e.getProject().getId() : null,
        e.getEmployeeNo(),
        e.getFirstName(),
        e.getLastName(),
        e.getJobTitle(),
        e.getProfession(),
        e.getPrimaryManager() != null ? e.getPrimaryManager().getId() : null,
        e.getUser() != null ? e.getUser().getId() : null,
        e.isEnabled()
    );
  }
}


