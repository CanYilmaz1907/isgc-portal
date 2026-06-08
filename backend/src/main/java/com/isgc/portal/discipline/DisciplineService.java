package com.isgc.portal.discipline;

import com.isgc.portal.discipline.dto.DisciplineResponse;
import com.isgc.portal.discipline.dto.DisciplineUpsertRequest;
import com.isgc.portal.employee.Employee;
import com.isgc.portal.employee.EmployeeRepository;
import com.isgc.portal.mail.MailProperties;
import com.isgc.portal.mail.MailService;
import com.isgc.portal.project.Project;
import com.isgc.portal.project.ProjectRepository;
import com.isgc.portal.security.AccessControlService;
import com.isgc.portal.security.CurrentUser;
import com.isgc.portal.user.Role;
import com.isgc.portal.user.RoleCapabilities;
import com.isgc.portal.user.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisciplineService {
  public static final String FILE_MODULE = "DISCIPLINE";
  private final DisciplineLogRepository repo;
  private final ProjectRepository projectRepo;
  private final EmployeeRepository employeeRepo;
  private final UserRepository userRepo;
  private final AccessControlService access;
  private final MailService mailService;
  private final MailProperties mailProps;

  public DisciplineService(
      DisciplineLogRepository repo,
      ProjectRepository projectRepo,
      EmployeeRepository employeeRepo,
      UserRepository userRepo,
      AccessControlService access,
      MailService mailService,
      MailProperties mailProps
  ) {
    this.repo = repo;
    this.projectRepo = projectRepo;
    this.employeeRepo = employeeRepo;
    this.userRepo = userRepo;
    this.access = access;
    this.mailService = mailService;
    this.mailProps = mailProps;
  }

  @Transactional(readOnly = true)
  public List<DisciplineResponse> list(CurrentUser user) {
    if (RoleCapabilities.canViewAll(user.role())) {
      return repo.findAll().stream().map(this::toDto).toList();
    }

    UUID myEmployeeId = access.requireEmployeeIdForUser(user.id());
    Employee me = employeeRepo.findById(myEmployeeId).orElseThrow();

    if (user.role() == Role.YONETICI) {
      UUID projectId = me.getProject() != null ? me.getProject().getId() : null;
      if (projectId == null) return List.of();
      return repo.findByProjectId(projectId).stream().map(this::toDto).toList();
    }

    List<DisciplineResponse> out = new ArrayList<>();
    for (DisciplineLog d : repo.findAll()) {
      if (d.getViolatingEmployee() != null && d.getViolatingEmployee().getId().equals(myEmployeeId)) {
        out.add(toDto(d));
      } else if (d.getEmployeeRegistrationNo() != null
          && d.getEmployeeRegistrationNo().equals(me.getEmployeeNo())) {
        out.add(toDto(d));
      }
    }
    return out;
  }

  @Transactional(readOnly = true)
  public DisciplineResponse get(CurrentUser user, UUID id) {
    DisciplineLog d = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Discipline log not found"));
    boolean allowed = list(user).stream().anyMatch(x -> x.id().equals(id));
    if (!allowed) throw new IllegalArgumentException("Access denied");
    return toDto(d);
  }

  @Transactional
  public DisciplineResponse create(CurrentUser user, DisciplineUpsertRequest req) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) {
      throw new IllegalArgumentException("Access denied");
    }
    validateViolationType(req.categoryLevel(), req.violationType());

    DisciplineLog d = new DisciplineLog();
    d.setId(UUID.randomUUID());
    d.setSequenceNo(repo.nextSequenceNo().intValue());
    applyRequest(d, req);
    d.setCreatedBy(userRepo.findById(user.id()).orElse(null));
    computeRepeatCount(d);
    repo.save(d);

    sendCreatedMail(d);
    if (d.getCategoryLevel() == DisciplineCategory.CAT_0) {
      sendCategoryZeroAlert(d);
    }
    return toDto(d);
  }

  @Transactional
  public DisciplineResponse update(CurrentUser user, UUID id, DisciplineUpsertRequest req) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) {
      throw new IllegalArgumentException("Access denied");
    }
    validateViolationType(req.categoryLevel(), req.violationType());

    DisciplineLog d = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Discipline log not found"));
    applyRequest(d, req);
    computeRepeatCount(d);
    repo.save(d);
    return toDto(d);
  }

  @Transactional
  public void delete(CurrentUser user, UUID id) {
    if (user.role() != Role.ADMIN) throw new IllegalArgumentException("Access denied");
    repo.deleteById(id);
  }

  @Transactional(readOnly = true)
  public DisciplineLog getEntityWithEmployees(CurrentUser user, UUID id) {
    get(user, id);
    return repo.findByIdWithEmployees(id).orElseThrow(() -> new IllegalArgumentException("Discipline log not found"));
  }

  @Transactional
  public int sendDailySummary() {
    var to = mailService.parseEmails(mailProps.managementCc());
    if (to.isEmpty()) return 0;
    long active = repo.findAll().stream()
        .filter(d -> d.getStatus() != DisciplineStatus.SOZLESME_FESHI)
        .count();
    mailService.sendTemplate(
        "Disiplin Logu - Günlük Özet",
        to,
        List.of(),
        "mail/discipline-reminder",
        Map.of(
            "employeeName", "-",
            "managerName", "-",
            "category", "Özet",
            "severity", String.valueOf(active),
            "description", "Aktif disiplin kaydı sayısı: " + active
        )
    );
    return 1;
  }

  private void applyRequest(DisciplineLog d, DisciplineUpsertRequest req) {
    if (req.projectId() != null) {
      d.setProject(projectRepo.findById(req.projectId()).orElseThrow(() -> new IllegalArgumentException("Project not found")));
    } else {
      d.setProject(null);
    }
    d.setOccurredAt(req.occurredAt());
    d.setFullName(req.fullName());
    d.setEmployeeRegistrationNo(req.employeeRegistrationNo());
    d.setCompany(req.company());
    d.setJobTitle(req.jobTitle());
    d.setWorkArea(req.workArea());
    d.setCategoryLevel(req.categoryLevel());
    d.setCategory(req.categoryLevel().name());
    d.setViolationType(req.violationType());
    d.setViolationDescription(req.violationDescription());
    d.setDescription(req.violationDescription());
    d.setResponsiblePerson(req.responsiblePerson());
    d.setStatus(req.status());
    d.setNotes(req.notes());
    d.setPenaltyAmount(req.penaltyAmount());
    d.setProfession(req.profession());

    if (req.violatingEmployeeId() != null) {
      Employee emp = employeeRepo.findById(req.violatingEmployeeId())
          .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
      d.setViolatingEmployee(emp);
      if (d.getFullName() == null || d.getFullName().isBlank()) {
        d.setFullName(emp.getFirstName() + " " + emp.getLastName());
      }
      if (d.getEmployeeRegistrationNo() == null || d.getEmployeeRegistrationNo().isBlank()) {
        d.setEmployeeRegistrationNo(emp.getEmployeeNo());
      }
      if (d.getJobTitle() == null || d.getJobTitle().isBlank()) {
        d.setJobTitle(emp.getJobTitle());
      }
      if (d.getCompany() == null || d.getCompany().isBlank()) {
        Project p = emp.getProject();
        if (p != null) d.setCompany(p.getName());
      }
    } else {
      d.setViolatingEmployee(null);
    }

    if (req.violatingManagerEmployeeId() != null) {
      d.setViolatingManagerEmployee(employeeRepo.findById(req.violatingManagerEmployeeId())
          .orElseThrow(() -> new IllegalArgumentException("Manager not found")));
    } else {
      d.setViolatingManagerEmployee(null);
    }
  }

  private void computeRepeatCount(DisciplineLog d) {
    String regNo = d.getEmployeeRegistrationNo();
    if (regNo == null || regNo.isBlank()) {
      d.setRepeatCount(1);
      return;
    }
    Instant sixMonthsAgo = d.getOccurredAt().minus(180, ChronoUnit.DAYS);
    long prior = repo.countByEmployeeRegistrationNoAndOccurredAtAfter(regNo, sixMonthsAgo);
    d.setRepeatCount((int) prior + 1);
  }

  private void validateViolationType(DisciplineCategory category, String violationType) {
    if (!DisciplineViolationTypes.isValidForCategory(category, violationType)) {
      throw new IllegalArgumentException("Invalid violation type for category");
    }
  }

  private boolean isRepeatThresholdReached(DisciplineLog d) {
    if (d.getCategoryLevel() == null) return false;
    int threshold = switch (d.getCategoryLevel()) {
      case CAT_0 -> 1;
      case CAT_1 -> 2;
      case CAT_2 -> 3;
      case CAT_3 -> 4;
    };
    return d.getRepeatCount() >= threshold;
  }

  private void sendCreatedMail(DisciplineLog d) {
    var to = new ArrayList<String>();
    if (d.getViolatingEmployee() != null && d.getViolatingEmployee().getUser() != null) {
      String email = d.getViolatingEmployee().getUser().getEmail();
      if (email != null && !email.isBlank()) to.add(email);
    }
    var cc = new ArrayList<>(mailService.parseEmails(mailProps.managementCc()));
    if (d.getViolatingManagerEmployee() != null && d.getViolatingManagerEmployee().getUser() != null) {
      String email = d.getViolatingManagerEmployee().getUser().getEmail();
      if (email != null && !email.isBlank()) cc.add(email);
    }
    if (to.isEmpty() && cc.isEmpty()) return;

    String emp = displayName(d);
    String mgr = d.getViolatingManagerEmployee() != null
        ? d.getViolatingManagerEmployee().getFirstName() + " " + d.getViolatingManagerEmployee().getLastName()
        : (d.getResponsiblePerson() != null ? d.getResponsiblePerson() : "-");
    mailService.sendTemplate(
        "Disiplin Logu - " + emp,
        to,
        cc,
        "mail/discipline-reminder",
        Map.of(
            "employeeName", emp,
            "managerName", mgr,
            "category", d.getCategoryLevel() != null ? d.getCategoryLevel().name() : "-",
            "severity", String.valueOf(d.getRepeatCount()),
            "description", d.getViolationDescription() != null ? d.getViolationDescription() : "-"
        )
    );
  }

  private void sendCategoryZeroAlert(DisciplineLog d) {
    var to = mailService.parseEmails(mailProps.managementCc());
    if (to.isEmpty()) return;
    mailService.sendTemplate(
        "ACİL: Kategori 0 Disiplin İhlali",
        to,
        List.of(),
        "mail/discipline-reminder",
        Map.of(
            "employeeName", displayName(d),
            "managerName", d.getResponsiblePerson() != null ? d.getResponsiblePerson() : "-",
            "category", "Kategori 0 - Direkt Çıkış",
            "severity", String.valueOf(d.getRepeatCount()),
            "description", d.getViolationDescription() != null ? d.getViolationDescription() : "-"
        )
    );
  }

  private String displayName(DisciplineLog d) {
    if (d.getFullName() != null && !d.getFullName().isBlank()) return d.getFullName();
    if (d.getViolatingEmployee() != null) {
      return d.getViolatingEmployee().getFirstName() + " " + d.getViolatingEmployee().getLastName();
    }
    return "-";
  }

  private DisciplineResponse toDto(DisciplineLog d) {
    return new DisciplineResponse(
        d.getId(),
        d.getSequenceNo(),
        d.getProject() != null ? d.getProject().getId() : null,
        d.getOccurredAt(),
        d.getFullName(),
        d.getEmployeeRegistrationNo(),
        d.getCompany(),
        d.getJobTitle(),
        d.getWorkArea(),
        d.getCategoryLevel(),
        d.getViolationType(),
        DisciplineViolationTypes.labelFor(d.getViolationType()),
        d.getViolationDescription(),
        d.getResponsiblePerson(),
        d.getStatus(),
        d.getNotes(),
        d.getRepeatCount(),
        isRepeatThresholdReached(d),
        d.getPenaltyAmount(),
        d.getSeverity(),
        d.getProfession(),
        d.getViolatingEmployee() != null ? d.getViolatingEmployee().getId() : null,
        d.getViolatingEmployee() != null
            ? d.getViolatingEmployee().getFirstName() + " " + d.getViolatingEmployee().getLastName()
            : null,
        d.getViolatingManagerEmployee() != null ? d.getViolatingManagerEmployee().getId() : null,
        d.getViolatingManagerEmployee() != null
            ? d.getViolatingManagerEmployee().getFirstName() + " " + d.getViolatingManagerEmployee().getLastName()
            : null
    );
  }
}
