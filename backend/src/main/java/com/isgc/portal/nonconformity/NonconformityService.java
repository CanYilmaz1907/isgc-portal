package com.isgc.portal.nonconformity;

import com.isgc.portal.employee.Employee;
import com.isgc.portal.employee.EmployeeRepository;
import com.isgc.portal.mail.MailProperties;
import com.isgc.portal.mail.MailService;
import com.isgc.portal.nonconformity.dto.NonconformityResponse;
import com.isgc.portal.nonconformity.dto.NonconformityUpsertRequest;
import com.isgc.portal.project.ProjectRepository;
import com.isgc.portal.security.AccessControlService;
import com.isgc.portal.security.CurrentUser;
import com.isgc.portal.user.Role;
import com.isgc.portal.user.RoleCapabilities;
import com.isgc.portal.user.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NonconformityService {
  public static final String FILE_MODULE = "NONCONFORMITY";

  private final NonconformityRepository repo;
  private final NonconformityTemplateRepository templateRepo;
  private final HazardClassRepository hazardRepo;
  private final ProjectRepository projectRepo;
  private final EmployeeRepository employeeRepo;
  private final UserRepository userRepo;
  private final AccessControlService access;
  private final MailService mailService;
  private final MailProperties mailProps;

  public NonconformityService(
      NonconformityRepository repo,
      NonconformityTemplateRepository templateRepo,
      HazardClassRepository hazardRepo,
      ProjectRepository projectRepo,
      EmployeeRepository employeeRepo,
      UserRepository userRepo,
      AccessControlService access,
      MailService mailService,
      MailProperties mailProps
  ) {
    this.repo = repo;
    this.templateRepo = templateRepo;
    this.hazardRepo = hazardRepo;
    this.projectRepo = projectRepo;
    this.employeeRepo = employeeRepo;
    this.userRepo = userRepo;
    this.access = access;
    this.mailService = mailService;
    this.mailProps = mailProps;
  }

  @Transactional(readOnly = true)
  public List<NonconformityResponse> list(CurrentUser user) {
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

    // PERSONEL: kendisine atanmış olanlar (sorumlu)
    List<NonconformityResponse> out = new ArrayList<>();
    for (Nonconformity n : repo.findAll()) {
      if (n.getResponsibleEmployee() != null && n.getResponsibleEmployee().getId().equals(myEmployeeId)) {
        out.add(toDto(n));
      }
    }
    return out;
  }

  @Transactional(readOnly = true)
  public NonconformityResponse get(CurrentUser user, UUID id) {
    Nonconformity n = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Nonconformity not found"));
    boolean allowed = list(user).stream().anyMatch(x -> x.id().equals(id));
    if (!allowed) throw new IllegalArgumentException("Access denied");
    return toDto(n);
  }

  @Transactional
  public NonconformityResponse create(CurrentUser user, NonconformityUpsertRequest req) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) {
      throw new IllegalArgumentException("Access denied");
    }

    NonconformityTemplate t = templateRepo.findById(req.templateId())
        .orElseThrow(() -> new IllegalArgumentException("Template not found"));

    Nonconformity n = new Nonconformity();
    n.setId(UUID.randomUUID());
    n.setTemplate(t);
    if (req.projectId() != null) {
      n.setProject(projectRepo.findById(req.projectId()).orElseThrow(() -> new IllegalArgumentException("Project not found")));
    }
    if (req.hazardClassId() != null) {
      n.setHazardClass(hazardRepo.findById(req.hazardClassId()).orElseThrow(() -> new IllegalArgumentException("Hazard class not found")));
    }
    if (req.responsibleEmployeeId() != null) {
      n.setResponsibleEmployee(employeeRepo.findById(req.responsibleEmployeeId()).orElseThrow(() -> new IllegalArgumentException("Employee not found")));
    }
    n.setAssignedBy(userRepo.findById(user.id()).orElse(null));
    n.setTitle(req.title());
    n.setDescription(req.description());
    n.setDueDate(req.dueDate());
    n.setStatus(req.status() != null ? req.status() : NonconformityStatus.OPEN);
    n.setSeverity(req.severity());
    n.setData(req.dataJson() == null || req.dataJson().isBlank() ? "{}" : req.dataJson());
    repo.save(n);
    return toDto(n);
  }

  @Transactional
  public NonconformityResponse update(CurrentUser user, UUID id, NonconformityUpsertRequest req) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) {
      throw new IllegalArgumentException("Access denied");
    }
    Nonconformity n = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Nonconformity not found"));

    if (req.projectId() != null) {
      n.setProject(projectRepo.findById(req.projectId()).orElseThrow(() -> new IllegalArgumentException("Project not found")));
    } else {
      n.setProject(null);
    }
    if (req.templateId() != null) {
      n.setTemplate(templateRepo.findById(req.templateId()).orElseThrow(() -> new IllegalArgumentException("Template not found")));
    }
    if (req.hazardClassId() != null) {
      n.setHazardClass(hazardRepo.findById(req.hazardClassId()).orElseThrow(() -> new IllegalArgumentException("Hazard class not found")));
    } else {
      n.setHazardClass(null);
    }
    if (req.responsibleEmployeeId() != null) {
      n.setResponsibleEmployee(employeeRepo.findById(req.responsibleEmployeeId()).orElseThrow(() -> new IllegalArgumentException("Employee not found")));
    } else {
      n.setResponsibleEmployee(null);
    }
    n.setTitle(req.title());
    n.setDescription(req.description());
    n.setDueDate(req.dueDate());
    if (req.status() != null) n.setStatus(req.status());
    n.setSeverity(req.severity());
    n.setData(req.dataJson() == null || req.dataJson().isBlank() ? "{}" : req.dataJson());
    repo.save(n);
    return toDto(n);
  }

  @Transactional
  public void delete(CurrentUser user, UUID id) {
    if (user.role() != Role.ADMIN) throw new IllegalArgumentException("Access denied");
    repo.deleteById(id);
  }

  @Transactional
  public int sendOpenReminders() {
    int sent = 0;
    Instant now = Instant.now();
    for (Nonconformity n : repo.findOpen()) {
      if (n.getResponsibleEmployee() == null) continue;
      if (n.getLastRemindedAt() != null && n.getLastRemindedAt().isAfter(now.minusSeconds(24 * 3600))) {
        continue; // daily throttle
      }
      // only if due soon/overdue
      LocalDate due = n.getDueDate();
      if (due != null) {
        LocalDate today = LocalDate.now();
        if (due.isAfter(today.plusDays(2))) {
          continue;
        }
      }

      var to = new ArrayList<String>();
      if (n.getResponsibleEmployee().getUser() != null && n.getResponsibleEmployee().getUser().getEmail() != null) {
        to.add(n.getResponsibleEmployee().getUser().getEmail());
      }
      var cc = mailService.parseEmails(mailProps.managementCc());
      if (to.isEmpty() && cc.isEmpty()) continue;

      String projectName = n.getProject() != null ? n.getProject().getName() : "-";
      mailService.sendTemplate(
          "Uygunsuzluk Hatırlatma - " + n.getTitle(),
          to,
          cc,
          "mail/nonconformity-reminder",
          Map.of(
              "title", n.getTitle(),
              "projectName", projectName,
              "status", n.getStatus().name(),
              "dueDate", n.getDueDate() != null ? n.getDueDate().toString() : "-",
              "description", n.getDescription() != null ? n.getDescription() : "-"
          )
      );
      n.setLastRemindedAt(now);
      repo.save(n);
      sent++;
    }
    return sent;
  }

  private NonconformityResponse toDto(Nonconformity n) {
    return new NonconformityResponse(
        n.getId(),
        n.getProject() != null ? n.getProject().getId() : null,
        n.getProject() != null ? n.getProject().getName() : null,
        n.getTemplate().getId(),
        n.getTemplate().getName(),
        n.getHazardClass() != null ? n.getHazardClass().getId() : null,
        n.getHazardClass() != null ? n.getHazardClass().getName() : null,
        n.getResponsibleEmployee() != null ? n.getResponsibleEmployee().getId() : null,
        n.getResponsibleEmployee() != null
            ? (n.getResponsibleEmployee().getFirstName() + " " + n.getResponsibleEmployee().getLastName())
            : null,
        n.getTitle(),
        n.getDescription(),
        n.getDueDate(),
        n.getStatus(),
        n.getSeverity(),
        n.getData()
    );
  }
}


