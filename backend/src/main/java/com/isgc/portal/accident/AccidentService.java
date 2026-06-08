package com.isgc.portal.accident;

import com.isgc.portal.accident.dto.AccidentResponse;
import com.isgc.portal.accident.dto.AccidentUpsertRequest;
import com.isgc.portal.accident.dto.CauseSelectionDto;
import com.isgc.portal.employee.Employee;
import com.isgc.portal.employee.EmployeeRepository;
import com.isgc.portal.mail.MailProperties;
import com.isgc.portal.mail.MailService;
import com.isgc.portal.project.ProjectRepository;
import com.isgc.portal.security.AccessControlService;
import com.isgc.portal.security.CurrentUser;
import com.isgc.portal.user.Role;
import com.isgc.portal.user.RoleCapabilities;
import com.isgc.portal.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccidentService {
  public static final String FILE_MODULE = "ACCIDENT";

  private final AccidentRepository accidentRepository;
  private final AccidentTypeRepository accidentTypeRepository;
  private final AccidentPersonRepository accidentPersonRepository;
  private final NotificationRuleRepository notificationRuleRepository;
  private final ProjectRepository projectRepository;
  private final EmployeeRepository employeeRepository;
  private final UserRepository userRepository;
  private final AccessControlService accessControlService;
  private final MailService mailService;
  private final MailProperties mailProperties;
  private final AccidentDirectCauseRepository directCauseRepository;
  private final AccidentRootCauseRepository rootCauseRepository;

  public AccidentService(
      AccidentRepository accidentRepository,
      AccidentTypeRepository accidentTypeRepository,
      AccidentPersonRepository accidentPersonRepository,
      NotificationRuleRepository notificationRuleRepository,
      ProjectRepository projectRepository,
      EmployeeRepository employeeRepository,
      UserRepository userRepository,
      AccessControlService accessControlService,
      MailService mailService,
      MailProperties mailProperties,
      AccidentDirectCauseRepository directCauseRepository,
      AccidentRootCauseRepository rootCauseRepository
  ) {
    this.accidentRepository = accidentRepository;
    this.accidentTypeRepository = accidentTypeRepository;
    this.accidentPersonRepository = accidentPersonRepository;
    this.notificationRuleRepository = notificationRuleRepository;
    this.projectRepository = projectRepository;
    this.employeeRepository = employeeRepository;
    this.userRepository = userRepository;
    this.accessControlService = accessControlService;
    this.mailService = mailService;
    this.mailProperties = mailProperties;
    this.directCauseRepository = directCauseRepository;
    this.rootCauseRepository = rootCauseRepository;
  }

  @Transactional(readOnly = true)
  public List<AccidentResponse> list(CurrentUser user) {
    if (RoleCapabilities.canViewAll(user.role())) {
      return accidentRepository.findAll().stream().map(this::toResponse).toList();
    }

    UUID myEmployeeId = accessControlService.requireEmployeeIdForUser(user.id());
    Employee me = employeeRepository.findById(myEmployeeId).orElseThrow();

    if (user.role() == Role.YONETICI) {
      UUID myProjectId = me.getProject() != null ? me.getProject().getId() : null;
      if (myProjectId == null) return List.of();
      return accidentRepository.findByProjectId(myProjectId).stream().map(this::toResponse).toList();
    }

    // PERSONEL: sadece kendisinin dahil olduğu kazalar (injured/key) veya kendi bildirdiği
    List<AccidentResponse> out = new ArrayList<>();
    for (Accident a : accidentRepository.findAll()) {
      boolean reportedByMe = a.getReportedBy() != null && a.getReportedBy().getId().equals(user.id());
      if (reportedByMe) {
        out.add(toResponse(a));
        continue;
      }
      var people = accidentPersonRepository.findByAccidentId(a.getId());
      boolean involved = people.stream().anyMatch(p -> p.getEmployee().getId().equals(myEmployeeId));
      if (involved) {
        out.add(toResponse(a));
      }
    }
    return out;
  }

  @Transactional(readOnly = true)
  public AccidentResponse get(CurrentUser user, UUID id) {
    Accident a = accidentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Accident not found"));
    // yetki: list() mantığı ile aynı; basit kontrol
    boolean allowed = list(user).stream().anyMatch(x -> x.id().equals(id));
    if (!allowed) throw new IllegalArgumentException("Access denied");
    return toResponse(a);
  }

  @Transactional(readOnly = true)
  public Accident getEntityWithRelations(CurrentUser user, UUID id) {
    get(user, id); // Access check
    Accident a = accidentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Accident not found"));
    // Trigger lazy load of relations needed for PDF in same transaction
    if (a.getAccidentType() != null) a.getAccidentType().getName();
    if (a.getProject() != null) a.getProject().getName();
    if (a.getSupervisorEmployee() != null) a.getSupervisorEmployee().getFirstName();
    return a;
  }

  @Transactional
  public AccidentResponse create(CurrentUser user, AccidentUpsertRequest req) {
    AccidentType type = accidentTypeRepository.findById(req.accidentTypeId())
        .orElseThrow(() -> new IllegalArgumentException("Accident type not found"));

    Accident a = new Accident();
    a.setId(UUID.randomUUID());
    a.setAccidentType(type);
    if (req.projectId() != null) {
      a.setProject(projectRepository.findById(req.projectId())
          .orElseThrow(() -> new IllegalArgumentException("Project not found")));
    }
    a.setReportedBy(userRepository.findById(user.id()).orElse(null));
    a.setOccurredAt(req.occurredAt());
    a.setLocation(req.location());
    a.setAccidentClass(req.accidentClass());
    a.setPotentialLevel(req.potentialLevel());
    a.setDescription(req.description());
    a.setFormData(req.formDataJson() == null || req.formDataJson().isBlank() ? "{}" : req.formDataJson());
    a.setRootCauseData(req.rootCauseDataJson() == null || req.rootCauseDataJson().isBlank() ? "{}" : req.rootCauseDataJson());
    a.setStatus(req.status() != null ? req.status() : AccidentStatus.OPEN);
    // Excel-based fields
    a.setArea(req.area());
    a.setHazardSource(req.hazardSource());
    a.setInjuredBodyPart(req.injuredBodyPart());
    a.setInjuryType(req.injuryType());
    a.setEmployeeRegistrationNo(req.employeeRegistrationNo());
    if (req.supervisorEmployeeId() != null) {
      a.setSupervisorEmployee(employeeRepository.findById(req.supervisorEmployeeId())
          .orElseThrow(() -> new IllegalArgumentException("Supervisor employee not found")));
    }
    a.setTimePeriod(req.timePeriod());
    // New report template fields
    a.setGroupCompanyName(req.groupCompanyName());
    a.setResponsiblePerson(req.responsiblePerson());
    a.setEstimatedCost(req.estimatedCost());
    a.setWorkRelated(req.workRelated() != null ? req.workRelated() : true);
    a.setWorkDuringAccident(req.workDuringAccident());
    a.setInjuredPersonAge(req.injuredPersonAge());
    a.setInjuredPersonProfession(req.injuredPersonProfession());
    a.setInjuredPersonGender(req.injuredPersonGender());
    a.setInjuredPersonNationality(req.injuredPersonNationality());
    a.setInjuredPersonCompany(req.injuredPersonCompany());
    a.setActionsTaken(req.actionsTakenJson() == null || req.actionsTakenJson().isBlank() ? "[]" : req.actionsTakenJson());
    if (req.preparedByUserId() != null) {
      a.setPreparedBy(userRepository.findById(req.preparedByUserId())
          .orElseThrow(() -> new IllegalArgumentException("Prepared by user not found")));
    }
    a.setPreparedAt(req.preparedAt());
    applyIncidentFields(a, req);
    if (a.getIncidentNo() == null) {
      a.setIncidentNo(accidentRepository.nextIncidentNo().intValue());
    }
    accidentRepository.save(a);

    upsertPeople(a, req.injuredEmployeeIds(), req.keyPersonEmployeeIds());
    upsertCauses(a, req.directCauses(), req.rootCauses(), req.rootCauseDataJson());

    sendAccidentCreatedMail(a);
    return toResponse(a);
  }

  @Transactional
  public AccidentResponse update(CurrentUser user, UUID id, AccidentUpsertRequest req) {
    Accident a = accidentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Accident not found"));
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) {
      throw new IllegalArgumentException("Access denied");
    }
    if (req.projectId() != null) {
      a.setProject(projectRepository.findById(req.projectId())
          .orElseThrow(() -> new IllegalArgumentException("Project not found")));
    } else {
      a.setProject(null);
    }
    if (req.accidentTypeId() != null) {
      a.setAccidentType(accidentTypeRepository.findById(req.accidentTypeId())
          .orElseThrow(() -> new IllegalArgumentException("Accident type not found")));
    }
    a.setOccurredAt(req.occurredAt());
    a.setLocation(req.location());
    a.setAccidentClass(req.accidentClass());
    a.setPotentialLevel(req.potentialLevel());
    a.setDescription(req.description());
    a.setFormData(req.formDataJson() == null || req.formDataJson().isBlank() ? "{}" : req.formDataJson());
    a.setRootCauseData(req.rootCauseDataJson() == null || req.rootCauseDataJson().isBlank() ? "{}" : req.rootCauseDataJson());
    if (req.status() != null) a.setStatus(req.status());
    // Excel-based fields
    a.setArea(req.area());
    a.setHazardSource(req.hazardSource());
    a.setInjuredBodyPart(req.injuredBodyPart());
    a.setInjuryType(req.injuryType());
    a.setEmployeeRegistrationNo(req.employeeRegistrationNo());
    if (req.supervisorEmployeeId() != null) {
      a.setSupervisorEmployee(employeeRepository.findById(req.supervisorEmployeeId())
          .orElseThrow(() -> new IllegalArgumentException("Supervisor employee not found")));
    } else {
      a.setSupervisorEmployee(null);
    }
    a.setTimePeriod(req.timePeriod());
    // New report template fields
    a.setGroupCompanyName(req.groupCompanyName());
    a.setResponsiblePerson(req.responsiblePerson());
    a.setEstimatedCost(req.estimatedCost());
    if (req.workRelated() != null) a.setWorkRelated(req.workRelated());
    a.setWorkDuringAccident(req.workDuringAccident());
    a.setInjuredPersonAge(req.injuredPersonAge());
    a.setInjuredPersonProfession(req.injuredPersonProfession());
    a.setInjuredPersonGender(req.injuredPersonGender());
    a.setInjuredPersonNationality(req.injuredPersonNationality());
    a.setInjuredPersonCompany(req.injuredPersonCompany());
    if (req.actionsTakenJson() != null && !req.actionsTakenJson().isBlank()) {
      a.setActionsTaken(req.actionsTakenJson());
    }
    if (req.preparedByUserId() != null) {
      a.setPreparedBy(userRepository.findById(req.preparedByUserId())
          .orElseThrow(() -> new IllegalArgumentException("Prepared by user not found")));
    } else {
      a.setPreparedBy(null);
    }
    a.setPreparedAt(req.preparedAt());
    applyIncidentFields(a, req);
    accidentRepository.save(a);

    upsertPeople(a, req.injuredEmployeeIds(), req.keyPersonEmployeeIds());
    upsertCauses(a, req.directCauses(), req.rootCauses(), req.rootCauseDataJson());
    return toResponse(a);
  }

  @Transactional
  public void delete(CurrentUser user, UUID id) {
    if (user.role() != Role.ADMIN) throw new IllegalArgumentException("Access denied");
    directCauseRepository.deleteByAccidentId(id);
    rootCauseRepository.deleteByAccidentId(id);
    accidentPersonRepository.deleteByAccidentId(id);
    accidentRepository.deleteById(id);
  }

  private void upsertPeople(Accident a, List<UUID> injured, List<UUID> keyPeople) {
    accidentPersonRepository.deleteByAccidentId(a.getId());
    if (injured != null) {
      for (UUID eid : injured) {
        AccidentPerson ap = new AccidentPerson();
        ap.setId(UUID.randomUUID());
        ap.setAccident(a);
        ap.setEmployee(employeeRepository.findById(eid).orElseThrow(() -> new IllegalArgumentException("Employee not found")));
        ap.setRole(AccidentPersonRole.INJURED);
        accidentPersonRepository.save(ap);
      }
    }
    if (keyPeople != null) {
      for (UUID eid : keyPeople) {
        AccidentPerson ap = new AccidentPerson();
        ap.setId(UUID.randomUUID());
        ap.setAccident(a);
        ap.setEmployee(employeeRepository.findById(eid).orElseThrow(() -> new IllegalArgumentException("Employee not found")));
        ap.setRole(AccidentPersonRole.KEY_PERSON);
        accidentPersonRepository.save(ap);
      }
    }
  }

  private void sendAccidentCreatedMail(Accident a) {
    try {
      var ruleOpt = notificationRuleRepository.findByAccidentClassAndPotentialLevelAndEnabledTrue(
          a.getAccidentClass(), a.getPotentialLevel());
      if (ruleOpt.isEmpty()) return;

      var rule = ruleOpt.get();
      var to = mailService.parseEmails(rule.getToEmails());
      var cc = new ArrayList<>(mailService.parseEmails(rule.getCcEmails()));
      var mgmt = mailService.parseEmails(mailProperties.managementCc());
      cc.addAll(mgmt);

      String projectName = a.getProject() != null ? a.getProject().getName() : "-";
      mailService.sendTemplate(
          "Kaza Bildirimi - " + a.getAccidentClass() + " / " + a.getPotentialLevel(),
          to,
          cc,
          "mail/accident-created",
          Map.of(
              "projectName", projectName,
              "accidentTypeName", a.getAccidentType().getName(),
              "accidentClass", a.getAccidentClass().name(),
              "potentialLevel", a.getPotentialLevel().name(),
              "location", a.getLocation() == null ? "-" : a.getLocation(),
              "description", a.getDescription() == null ? "-" : a.getDescription()
          )
      );
    } catch (Exception e) {
      // Log but don't fail the transaction
      org.slf4j.LoggerFactory.getLogger(AccidentService.class).warn("Failed to send accident notification email", e);
    }
  }

  private AccidentResponse toResponse(Accident a) {
    var people = accidentPersonRepository.findByAccidentId(a.getId());
    var injured = people.stream()
        .filter(p -> p.getRole() == AccidentPersonRole.INJURED)
        .map(p -> new AccidentResponse.PersonRef(p.getEmployee().getId(), p.getEmployee().getFirstName(), p.getEmployee().getLastName()))
        .toList();
    var key = people.stream()
        .filter(p -> p.getRole() == AccidentPersonRole.KEY_PERSON)
        .map(p -> new AccidentResponse.PersonRef(p.getEmployee().getId(), p.getEmployee().getFirstName(), p.getEmployee().getLastName()))
        .toList();
    AccidentResponse.PersonRef supervisorRef = null;
    if (a.getSupervisorEmployee() != null) {
      supervisorRef = new AccidentResponse.PersonRef(
          a.getSupervisorEmployee().getId(),
          a.getSupervisorEmployee().getFirstName(),
          a.getSupervisorEmployee().getLastName()
      );
    }
    AccidentResponse.PersonRef preparedByRef = null;
    if (a.getPreparedBy() != null) {
      preparedByRef = new AccidentResponse.PersonRef(
          a.getPreparedBy().getId(),
          a.getPreparedBy().getUsername(), // User doesn't have firstName/lastName, use username
          "" // Empty last name
      );
    }
    return new AccidentResponse(
        a.getId(),
        a.getProject() != null ? a.getProject().getId() : null,
        a.getProject() != null ? a.getProject().getName() : null,
        a.getAccidentType().getId(),
        a.getAccidentType().getName(),
        a.getOccurredAt(),
        a.getLocation(),
        a.getAccidentClass(),
        a.getPotentialLevel(),
        a.getDescription(),
        a.getFormData(),
        a.getRootCauseData(),
        a.getStatus(),
        injured,
        key,
        // Excel-based fields
        a.getArea(),
        a.getHazardSource(),
        a.getInjuredBodyPart(),
        a.getInjuryType(),
        a.getEmployeeRegistrationNo(),
        supervisorRef,
        a.getTimePeriod(),
        // New report template fields
        a.getGroupCompanyName(),
        a.getResponsiblePerson(),
        a.getEstimatedCost(),
        a.getWorkRelated(),
        a.getWorkDuringAccident(),
        a.getInjuredPersonAge(),
        a.getInjuredPersonProfession(),
        a.getInjuredPersonGender(),
        a.getInjuredPersonNationality(),
        a.getInjuredPersonCompany(),
        a.getActionsTaken(),
        preparedByRef,
        a.getPreparedAt(),
        a.getIncidentNo(),
        a.getClassification(),
        a.getPersonName(),
        a.getDurationOnProject(),
        a.getDurationInRole(),
        a.getWorkSupervisor(),
        a.getEmergencyNotificationSent(),
        a.getVehiclePlate(),
        a.getVehicleType(),
        loadDirectCauses(a.getId()),
        loadRootCauses(a.getId())
    );
  }

  private void applyIncidentFields(Accident a, AccidentUpsertRequest req) {
    if (req.classification() != null && !req.classification().isBlank()) {
      a.setClassification(req.classification());
      a.setAccidentClass(mapClassificationToAccidentClass(req.classification()));
    }
    a.setPersonName(req.personName());
    a.setDurationOnProject(req.durationOnProject());
    a.setDurationInRole(req.durationInRole());
    a.setWorkSupervisor(req.workSupervisor());
    a.setEmergencyNotificationSent(req.emergencyNotificationSent());
    a.setVehiclePlate(req.vehiclePlate());
    a.setVehicleType(req.vehicleType());
    if (req.employeeRegistrationNo() != null) {
      a.setEmployeeRegistrationNo(req.employeeRegistrationNo());
    }
    if (req.timePeriod() != null) {
      a.setTimePeriod(req.timePeriod());
    }
  }

  private AccidentClass mapClassificationToAccidentClass(String classification) {
    return switch (classification.toUpperCase()) {
      case "FAT" -> AccidentClass.FATAL;
      case "LTI", "PERMANENT_DISABILITY", "RWC" -> AccidentClass.MAJOR;
      case "NEAR_MISS" -> AccidentClass.NEAR_MISS;
      default -> AccidentClass.MINOR;
    };
  }

  private void upsertCauses(
      Accident a,
      List<CauseSelectionDto> directCauses,
      List<CauseSelectionDto> rootCauses,
      String rootCauseDataJson
  ) {
    directCauseRepository.deleteByAccidentId(a.getId());
    rootCauseRepository.deleteByAccidentId(a.getId());

    if (directCauses != null) {
      for (CauseSelectionDto c : directCauses) {
        if (c == null || c.code() == null || c.code().isBlank()) continue;
        AccidentDirectCause dc = new AccidentDirectCause();
        dc.setId(UUID.randomUUID());
        dc.setAccident(a);
        dc.setCauseCode(c.code());
        dc.setCauseLabel(c.label() != null ? c.label() : c.code());
        directCauseRepository.save(dc);
      }
    }
    if (rootCauses != null) {
      for (CauseSelectionDto c : rootCauses) {
        if (c == null || c.code() == null || c.code().isBlank()) continue;
        AccidentRootCause rc = new AccidentRootCause();
        rc.setId(UUID.randomUUID());
        rc.setAccident(a);
        rc.setCauseCode(c.code());
        rc.setCauseLabel(c.label() != null ? c.label() : c.code());
        rootCauseRepository.save(rc);
      }
    }

    if ((directCauses == null || directCauses.isEmpty()) && (rootCauses == null || rootCauses.isEmpty())) {
      if (rootCauseDataJson != null && !rootCauseDataJson.isBlank()) {
        a.setRootCauseData(rootCauseDataJson);
      }
    } else if (directCauses != null || rootCauses != null) {
      a.setRootCauseData(buildLegacyRootCauseJson(directCauses, rootCauses));
      accidentRepository.save(a);
    }
  }

  private String buildLegacyRootCauseJson(List<CauseSelectionDto> direct, List<CauseSelectionDto> root) {
    List<String> behavior = new ArrayList<>();
    List<String> condition = new ArrayList<>();
    List<String> personal = new ArrayList<>();
    List<String> work = new ArrayList<>();
    if (direct != null) {
      for (CauseSelectionDto c : direct) {
        if (c == null) continue;
        String label = c.label() != null ? c.label() : c.code();
        if (c.code() != null && c.code().startsWith("1-")) behavior.add(label);
        else if (c.code() != null && (c.code().startsWith("5-") || c.code().startsWith("6-")
            || c.code().startsWith("7-") || c.code().startsWith("8-"))) condition.add(label);
        else behavior.add(label);
      }
    }
    if (root != null) {
      for (CauseSelectionDto c : root) {
        if (c == null) continue;
        String label = c.label() != null ? c.label() : c.code();
        if (c.code() != null) {
          int group = parseGroupCode(c.code());
          if (group >= 1 && group <= 5) personal.add(label);
          else work.add(label);
        }
      }
    }
    return String.format(
        "{\"unsafeBehavior\":%s,\"unsafeCondition\":%s,\"personalFactors\":%s,\"workFactors\":%s}",
        toJsonArray(behavior), toJsonArray(condition), toJsonArray(personal), toJsonArray(work));
  }

  private int parseGroupCode(String code) {
    int dash = code.indexOf('-');
    if (dash <= 0) return 0;
    try {
      return Integer.parseInt(code.substring(0, dash));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private String toJsonArray(List<String> items) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < items.size(); i++) {
      if (i > 0) sb.append(',');
      sb.append('"').append(items.get(i).replace("\"", "\\\"")).append('"');
    }
    sb.append(']');
    return sb.toString();
  }

  private List<CauseSelectionDto> loadDirectCauses(UUID accidentId) {
    return directCauseRepository.findByAccidentId(accidentId).stream()
        .map(c -> new CauseSelectionDto(c.getCauseCode(), c.getCauseLabel()))
        .toList();
  }

  private List<CauseSelectionDto> loadRootCauses(UUID accidentId) {
    return rootCauseRepository.findByAccidentId(accidentId).stream()
        .map(c -> new CauseSelectionDto(c.getCauseCode(), c.getCauseLabel()))
        .toList();
  }
}


