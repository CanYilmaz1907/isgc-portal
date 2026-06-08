package com.isgc.portal.training;

import com.isgc.portal.employee.EmployeeRepository;
import com.isgc.portal.security.AccessControlService;
import com.isgc.portal.security.CurrentUser;
import com.isgc.portal.training.dto.TrainingRecordRequest;
import com.isgc.portal.training.dto.TrainingRecordResponse;
import com.isgc.portal.user.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/training-records")
public class TrainingRecordController {
  private final TrainingRecordRepository trainingRecordRepository;
  private final EmployeeRepository employeeRepository;
  private final AccessControlService accessControlService;

  public TrainingRecordController(
      TrainingRecordRepository trainingRecordRepository,
      EmployeeRepository employeeRepository,
      AccessControlService accessControlService
  ) {
    this.trainingRecordRepository = trainingRecordRepository;
    this.employeeRepository = employeeRepository;
    this.accessControlService = accessControlService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<TrainingRecordResponse> list(@AuthenticationPrincipal @NotNull CurrentUser user) {
    if (user.role() == Role.ADMIN || user.role() == Role.ISG_C) {
      return trainingRecordRepository.findAll().stream().map(this::toResponse).toList();
    }
    UUID myEmployeeId = accessControlService.requireEmployeeIdForUser(user.id());
    if (user.role() == Role.PERSONEL) {
      return trainingRecordRepository.findByEmployeeId(myEmployeeId).stream().map(this::toResponse).toList();
    }
    // YONETICI: kendi + direkt raporlar
    var direct = employeeRepository.findDirectReports(myEmployeeId).stream().map(e -> e.getId()).toList();
    return trainingRecordRepository.findAll().stream()
        .filter(t -> t.getEmployee().getId().equals(myEmployeeId) || direct.contains(t.getEmployee().getId()))
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/by-employee/{employeeId}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<TrainingRecordResponse> byEmployee(
      @PathVariable UUID employeeId,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    if (!accessControlService.canAccessEmployee(user, employeeId)) {
      throw new IllegalArgumentException("Access denied");
    }
    return trainingRecordRepository.findByEmployeeId(employeeId).stream().map(this::toResponse).toList();
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public TrainingRecordResponse create(@Valid @RequestBody TrainingRecordRequest req) {
    if (req.employeeId() == null) throw new IllegalArgumentException("employeeId required");
    TrainingRecord tr = new TrainingRecord();
    tr.setId(UUID.randomUUID());
    tr.setEmployee(employeeRepository.findById(req.employeeId())
        .orElseThrow(() -> new IllegalArgumentException("Employee not found")));
    tr.setTrainingName(req.trainingName());
    tr.setProvider(req.provider());
    tr.setCompletedOn(req.completedOn());
    tr.setValidUntil(req.validUntil());
    trainingRecordRepository.save(tr);
    return toResponse(tr);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public TrainingRecordResponse update(@PathVariable UUID id, @Valid @RequestBody TrainingRecordRequest req) {
    TrainingRecord tr = trainingRecordRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Training record not found"));
    if (req.employeeId() != null) {
      tr.setEmployee(employeeRepository.findById(req.employeeId())
          .orElseThrow(() -> new IllegalArgumentException("Employee not found")));
    }
    tr.setTrainingName(req.trainingName());
    tr.setProvider(req.provider());
    tr.setCompletedOn(req.completedOn());
    tr.setValidUntil(req.validUntil());
    trainingRecordRepository.save(tr);
    return toResponse(tr);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable UUID id) {
    trainingRecordRepository.deleteById(id);
  }

  private TrainingRecordResponse toResponse(TrainingRecord t) {
    return new TrainingRecordResponse(
        t.getId(),
        t.getEmployee().getId(),
        t.getTrainingName(),
        t.getProvider(),
        t.getCompletedOn(),
        t.getValidUntil()
    );
  }
}


