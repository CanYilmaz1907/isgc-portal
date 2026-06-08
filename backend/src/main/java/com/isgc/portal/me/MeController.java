package com.isgc.portal.me;

import com.isgc.portal.employee.EmployeeRepository;
import com.isgc.portal.security.CurrentUser;
import com.isgc.portal.training.TrainingRecordRepository;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {
  private final EmployeeRepository employeeRepository;
  private final TrainingRecordRepository trainingRecordRepository;

  public MeController(EmployeeRepository employeeRepository, TrainingRecordRepository trainingRecordRepository) {
    this.employeeRepository = employeeRepository;
    this.trainingRecordRepository = trainingRecordRepository;
  }

  @GetMapping
  public MeResponse me(@AuthenticationPrincipal @NotNull CurrentUser user) {
    return new MeResponse(user.id(), user.username(), user.role().name());
  }

  @GetMapping("/employee")
  public EmployeeSummary myEmployee(@AuthenticationPrincipal @NotNull CurrentUser user) {
    return employeeRepository.findByUserId(user.id())
        .map(e -> new EmployeeSummary(
            e.getId(),
            e.getEmployeeNo(),
            e.getFirstName(),
            e.getLastName(),
            e.getJobTitle(),
            e.getProfession(),
            e.getProject() != null ? e.getProject().getId() : null
        ))
        .orElseThrow(() -> new IllegalArgumentException("Employee record not found for user"));
  }

  @GetMapping("/training-records")
  public List<TrainingRecordSummary> myTraining(@AuthenticationPrincipal @NotNull CurrentUser user) {
    UUID employeeId = employeeRepository.findByUserId(user.id())
        .map(e -> e.getId())
        .orElseThrow(() -> new IllegalArgumentException("Employee record not found for user"));

    return trainingRecordRepository.findByEmployeeId(employeeId).stream()
        .map(t -> new TrainingRecordSummary(
            t.getId(),
            t.getTrainingName(),
            t.getProvider(),
            t.getCompletedOn(),
            t.getValidUntil()
        ))
        .toList();
  }
}


