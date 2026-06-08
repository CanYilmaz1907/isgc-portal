package com.isgc.portal.ncr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isgc.portal.employee.Employee;
import com.isgc.portal.employee.EmployeeRepository;
import com.isgc.portal.ncr.dto.NcrResponse;
import com.isgc.portal.ncr.dto.NcrUpsertRequest;
import com.isgc.portal.project.ProjectRepository;
import com.isgc.portal.security.AccessControlService;
import com.isgc.portal.security.CurrentUser;
import com.isgc.portal.user.Role;
import com.isgc.portal.user.RoleCapabilities;
import com.isgc.portal.user.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NcrService implements NcrQueries {
  public static final String FILE_MODULE = "NCR";

  private final NcrRepository repo;
  private final ProjectRepository projectRepo;
  private final EmployeeRepository employeeRepo;
  private final UserRepository userRepo;
  private final AccessControlService access;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public NcrService(
      NcrRepository repo,
      ProjectRepository projectRepo,
      EmployeeRepository employeeRepo,
      UserRepository userRepo,
      AccessControlService access
  ) {
    this.repo = repo;
    this.projectRepo = projectRepo;
    this.employeeRepo = employeeRepo;
    this.userRepo = userRepo;
    this.access = access;
  }

  @Transactional(readOnly = true)
  public List<NcrResponse> list(CurrentUser user) {
    if (RoleCapabilities.canViewAll(user.role())) {
      return repo.findAll().stream().map(this::toDto).toList();
    }
    if (user.role() == Role.YONETICI) {
      UUID myEmployeeId = access.requireEmployeeIdForUser(user.id());
      Employee me = employeeRepo.findById(myEmployeeId).orElseThrow();
      UUID projectId = me.getProject() != null ? me.getProject().getId() : null;
      if (projectId == null) return List.of();
      return repo.findByProjectId(projectId).stream().map(this::toDto).toList();
    }
    return new ArrayList<>();
  }

  @Transactional(readOnly = true)
  public NcrResponse get(CurrentUser user, UUID id) {
    Ncr n = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("NCR not found"));
    boolean allowed = list(user).stream().anyMatch(x -> x.id().equals(id));
    if (!allowed) throw new IllegalArgumentException("Access denied");
    return toDto(n);
  }

  @Transactional(readOnly = true)
  public Ncr getEntity(UUID id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("NCR not found"));
  }

  @Transactional
  public NcrResponse create(CurrentUser user, NcrUpsertRequest req) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) {
      throw new IllegalArgumentException("Access denied");
    }
    LocalDate ncrDate = req.ncrDate() != null ? req.ncrDate() : LocalDate.now();

    Ncr entity = new Ncr();
    entity.setId(UUID.randomUUID());
    entity.setNcrNumber(generateNcrNumber(ncrDate));
    entity.setNcrDate(ncrDate);
    applyRequest(entity, req);
    entity.setAssignedBy(userRepo.findById(user.id()).orElse(null));
    if (entity.getInitiatedBy() == null || entity.getInitiatedBy().isBlank()) {
      entity.setInitiatedBy(user.username());
    }
    repo.save(entity);
    return toDto(entity);
  }

  @Transactional
  public NcrResponse update(CurrentUser user, UUID id, NcrUpsertRequest req) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) {
      throw new IllegalArgumentException("Access denied");
    }
    Ncr n = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("NCR not found"));
    applyRequest(n, req);
    repo.save(n);
    return toDto(n);
  }

  @Transactional
  public void delete(CurrentUser user, UUID id) {
    if (user.role() != Role.ADMIN) throw new IllegalArgumentException("Access denied");
    repo.deleteById(id);
  }

  private void applyRequest(Ncr entity, NcrUpsertRequest req) {
    if (req.projectId() != null) {
      entity.setProject(projectRepo.findById(req.projectId()).orElseThrow(() -> new IllegalArgumentException("Project not found")));
    } else {
      entity.setProject(null);
    }
    entity.setResponsibleOrganization(req.responsibleOrganization());
    entity.setLocation(req.location());
    entity.setTitle(req.title());
    entity.setDescription(req.description());
    entity.setEvidenceReferences(req.evidenceReferences());
    entity.setProposedCorrectiveAction(req.proposedCorrectiveAction());
    entity.setExecutedCorrectiveAction(req.executedCorrectiveAction());
    entity.setTargetCompletionDate(req.targetCompletionDate());
    entity.setCompletionDate(req.completionDate());
    entity.setDueDate(req.targetCompletionDate());
    entity.setClosedDate(req.completionDate());
    entity.setCorrectiveAction(req.proposedCorrectiveAction());
    entity.setRootCauseCategories(toJson(req.rootCauseCategories()));
    entity.setStatus(req.status() != null ? req.status() : NcrStatus.OPEN);
    entity.setInitiatedBy(req.initiatedBy());
    entity.setApprovedBy(req.approvedBy());
    entity.setVerifiedBy(req.verifiedBy());
    entity.setVerificationStatus(req.verificationStatus());
    entity.setIsoStandards(toJson(req.isoStandards()));
    entity.setFollowupRequired(req.followupRequired());
    entity.setNotes(req.notes());
    entity.setClassification(req.classification());
    entity.setRootCause(req.rootCause());
    entity.setPreventiveAction(req.preventiveAction());
    if (req.responsibleEmployeeId() != null) {
      entity.setResponsibleEmployee(employeeRepo.findById(req.responsibleEmployeeId())
          .orElseThrow(() -> new IllegalArgumentException("Employee not found")));
    } else {
      entity.setResponsibleEmployee(null);
    }
  }

  private String generateNcrNumber(LocalDate ncrDate) {
    int year = ncrDate.getYear();
    long count = repo.countByNcrDateBetween(LocalDate.of(year, 1, 1), LocalDate.of(year + 1, 1, 1));
    return String.format("NCR-%d-%03d", year, count + 1);
  }

  private String toJson(List<String> values) {
    try {
      return objectMapper.writeValueAsString(values != null ? values : List.of());
    } catch (JsonProcessingException e) {
      return "[]";
    }
  }

  private List<String> fromJson(String json) {
    if (json == null || json.isBlank()) return List.of();
    try {
      return objectMapper.readValue(json, new TypeReference<List<String>>() {});
    } catch (JsonProcessingException e) {
      return List.of();
    }
  }

  private boolean isOverdue(Ncr n) {
    if (n.getStatus() == NcrStatus.CLOSED) return false;
    LocalDate target = n.getTargetCompletionDate() != null ? n.getTargetCompletionDate() : n.getDueDate();
    return target != null && target.isBefore(LocalDate.now());
  }

  private NcrResponse toDto(Ncr n) {
    return new NcrResponse(
        n.getId(),
        n.getNcrNumber(),
        n.getNcrDate(),
        n.getProject() != null ? n.getProject().getId() : null,
        n.getProject() != null ? n.getProject().getName() : null,
        n.getResponsibleOrganization(),
        n.getLocation(),
        n.getTitle(),
        n.getDescription(),
        n.getEvidenceReferences(),
        n.getProposedCorrectiveAction(),
        n.getExecutedCorrectiveAction(),
        n.getTargetCompletionDate(),
        n.getCompletionDate(),
        fromJson(n.getRootCauseCategories()),
        n.getStatus(),
        n.getInitiatedBy(),
        n.getApprovedBy(),
        n.getVerifiedBy(),
        n.getVerificationStatus(),
        fromJson(n.getIsoStandards()),
        n.isFollowupRequired(),
        n.getNotes(),
        n.getResponsibleEmployee() != null ? n.getResponsibleEmployee().getId() : null,
        n.getResponsibleEmployee() != null
            ? (n.getResponsibleEmployee().getFirstName() + " " + n.getResponsibleEmployee().getLastName())
            : null,
        n.getClassification(),
        n.getRootCause(),
        n.getPreventiveAction(),
        isOverdue(n),
        n.getCreatedAt(),
        n.getUpdatedAt()
    );
  }
}
