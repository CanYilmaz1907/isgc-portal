package com.isgc.portal.audit;

import com.isgc.portal.audit.dto.AuditCreateRequest;
import com.isgc.portal.audit.dto.AuditResponse;
import com.isgc.portal.audit.dto.AuditSubmitRequest;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private final AuditRepository auditRepo;
  private final ChecklistRepository checklistRepo;
  private final ChecklistItemRepository itemRepo;
  private final AuditItemResultRepository resultRepo;
  private final AuditParticipantRepository participantRepo;
  private final ProjectRepository projectRepo;
  private final EmployeeRepository employeeRepo;
  private final UserRepository userRepo;
  private final AccessControlService access;
  private final MailService mailService;
  private final MailProperties mailProps;

  public AuditService(
      AuditRepository auditRepo,
      ChecklistRepository checklistRepo,
      ChecklistItemRepository itemRepo,
      AuditItemResultRepository resultRepo,
      AuditParticipantRepository participantRepo,
      ProjectRepository projectRepo,
      EmployeeRepository employeeRepo,
      UserRepository userRepo,
      AccessControlService access,
      MailService mailService,
      MailProperties mailProps
  ) {
    this.auditRepo = auditRepo;
    this.checklistRepo = checklistRepo;
    this.itemRepo = itemRepo;
    this.resultRepo = resultRepo;
    this.participantRepo = participantRepo;
    this.projectRepo = projectRepo;
    this.employeeRepo = employeeRepo;
    this.userRepo = userRepo;
    this.access = access;
    this.mailService = mailService;
    this.mailProps = mailProps;
  }

  @Transactional(readOnly = true)
  public List<AuditResponse> list(CurrentUser user) {
    if (RoleCapabilities.canViewAll(user.role())) {
      return auditRepo.findAll().stream().map(this::toDto).toList();
    }
    // YONETICI: proje bazlı, PERSONEL: yok (şimdilik)
    if (user.role() == Role.YONETICI) {
      UUID myEmployeeId = access.requireEmployeeIdForUser(user.id());
      var me = employeeRepo.findById(myEmployeeId).orElseThrow();
      UUID projectId = me.getProject() != null ? me.getProject().getId() : null;
      if (projectId == null) return List.of();
      return auditRepo.findByProjectId(projectId).stream().map(this::toDto).toList();
    }
    return List.of();
  }

  @Transactional(readOnly = true)
  public AuditResponse get(CurrentUser user, UUID id) {
    Audit a = auditRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Audit not found"));
    boolean allowed = list(user).stream().anyMatch(x -> x.id().equals(id));
    if (!allowed) throw new IllegalArgumentException("Access denied");
    return toDto(a);
  }

  @Transactional
  public AuditResponse create(CurrentUser user, AuditCreateRequest req) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) throw new IllegalArgumentException("Access denied");
    Audit a = new Audit();
    a.setId(UUID.randomUUID());
    if (req.projectId() != null) {
      a.setProject(projectRepo.findById(req.projectId()).orElseThrow(() -> new IllegalArgumentException("Project not found")));
    }
    a.setAuditType(req.auditType());
    if (req.checklistId() != null) {
      a.setChecklist(checklistRepo.findById(req.checklistId()).orElseThrow(() -> new IllegalArgumentException("Checklist not found")));
    }
    a.setTitle(req.title());
    a.setSummary(req.summary());
    a.setStatus(AuditStatus.DRAFT);
    a.setCreatedBy(userRepo.findById(user.id()).orElse(null));
    auditRepo.save(a);

    upsertParticipants(a, req.participants());
    return toDto(a);
  }

  @Transactional
  public AuditResponse update(CurrentUser user, UUID id, AuditCreateRequest req) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) throw new IllegalArgumentException("Access denied");
    Audit a = auditRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Audit not found"));
    if (a.getStatus() != AuditStatus.DRAFT) {
      throw new IllegalArgumentException("Can only update DRAFT audits");
    }
    if (req.projectId() != null) {
      a.setProject(projectRepo.findById(req.projectId()).orElseThrow(() -> new IllegalArgumentException("Project not found")));
    } else {
      a.setProject(null);
    }
    a.setAuditType(req.auditType());
    if (req.checklistId() != null) {
      a.setChecklist(checklistRepo.findById(req.checklistId()).orElseThrow(() -> new IllegalArgumentException("Checklist not found")));
    } else {
      a.setChecklist(null);
    }
    a.setTitle(req.title());
    a.setSummary(req.summary());
    auditRepo.save(a);

    upsertParticipants(a, req.participants());
    return toDto(a);
  }

  @Transactional
  public AuditResponse start(CurrentUser user, UUID id) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) throw new IllegalArgumentException("Access denied");
    Audit a = auditRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Audit not found"));
    a.setStatus(AuditStatus.IN_PROGRESS);
    if (a.getStartedAt() == null) a.setStartedAt(Instant.now());
    auditRepo.save(a);
    return toDto(a);
  }

  @Transactional
  public AuditResponse submitAndComplete(CurrentUser user, UUID id, AuditSubmitRequest req) {
    if (user.role() != Role.ADMIN && user.role() != Role.ISG_C) throw new IllegalArgumentException("Access denied");
    Audit a = auditRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Audit not found"));
    if (a.getChecklist() == null) throw new IllegalArgumentException("Checklist required");

    if (req.summary() != null) a.setSummary(req.summary());

    // upsert item results
    for (AuditSubmitRequest.ItemScore item : req.items()) {
      ChecklistItem ci = itemRepo.findById(item.checklistItemId())
          .orElseThrow(() -> new IllegalArgumentException("Checklist item not found"));
      if (!ci.getChecklist().getId().equals(a.getChecklist().getId())) {
        throw new IllegalArgumentException("Item not in checklist");
      }
      BigDecimal max = ci.getMaxScore();
      if (item.score().compareTo(BigDecimal.ZERO) < 0 || item.score().compareTo(max) > 0) {
        throw new IllegalArgumentException("Score out of range");
      }

      AuditItemResult r = resultRepo.findByAuditIdAndItemId(id, ci.getId()).orElseGet(() -> {
        AuditItemResult nr = new AuditItemResult();
        nr.setId(UUID.randomUUID());
        nr.setAudit(a);
        nr.setChecklistItem(ci);
        return nr;
      });
      r.setScore(item.score());
      r.setApplicable(item.applicable() == null ? true : item.applicable());
      r.setNote(item.note());
      resultRepo.save(r);
    }

    // calculate score
    var items = itemRepo.findByChecklistId(a.getChecklist().getId());
    var results = resultRepo.findByAuditId(id);
    var parentsWithChildren = AuditAnalysisService.categoriesWithChildren(items);

    BigDecimal denom = BigDecimal.ZERO;
    BigDecimal numer = BigDecimal.ZERO;
    for (ChecklistItem ci : items) {
      if (!ci.isEnabled()) continue;
      if (parentsWithChildren.contains(ci.getItemNo())) continue;

      BigDecimal w = ci.getWeight();
      AuditItemResult matched = results.stream()
          .filter(r -> r.getChecklistItem().getId().equals(ci.getId()))
          .findFirst()
          .orElse(null);

      boolean applicable = matched == null || matched.isApplicable();
      if (!applicable) continue;

      denom = denom.add(ci.getMaxScore().multiply(w));
      BigDecimal s = matched != null ? matched.getScore() : BigDecimal.ZERO;
      numer = numer.add(s.multiply(w));
    }
    BigDecimal percent = denom.compareTo(BigDecimal.ZERO) == 0
        ? BigDecimal.ZERO
        : numer.multiply(BigDecimal.valueOf(100)).divide(denom, 2, RoundingMode.HALF_UP);

    a.setCalculatedScore(percent);
    a.setStatus(AuditStatus.COMPLETED);
    if (a.getStartedAt() == null) a.setStartedAt(Instant.now());
    a.setFinishedAt(Instant.now());

    String report = buildReportHtml(a, items, results, percent);
    a.setReportHtml(report);
    auditRepo.save(a);

    sendCompletedMail(a);
    return toDto(a);
  }

  private void upsertParticipants(Audit a, List<AuditCreateRequest.Participant> participants) {
    participantRepo.deleteByAuditId(a.getId());
    if (participants == null) return;
    for (AuditCreateRequest.Participant p : participants) {
      AuditParticipant ap = new AuditParticipant();
      ap.setId(UUID.randomUUID());
      ap.setAudit(a);
      if (p.employeeId() != null) {
        ap.setEmployee(employeeRepo.findById(p.employeeId()).orElseThrow(() -> new IllegalArgumentException("Employee not found")));
      }
      ap.setRole(p.role());
      participantRepo.save(ap);
    }
  }

  private void sendCompletedMail(Audit a) {
    var to = new ArrayList<String>();
    Project p = a.getProject();
    if (p != null && p.getAuditNotificationEmails() != null) {
      to.addAll(mailService.parseEmails(p.getAuditNotificationEmails()));
    }
    if (to.isEmpty()) return;
    var cc = mailService.parseEmails(mailProps.managementCc());
    String projectName = p != null ? p.getName() : "-";
    mailService.sendTemplate(
        "Denetim Tamamlandı - " + a.getTitle(),
        to,
        cc,
        "mail/audit-completed",
        Map.of(
            "projectName", projectName,
            "title", a.getTitle(),
            "auditType", a.getAuditType().name(),
            "score", a.getCalculatedScore() != null ? a.getCalculatedScore().toPlainString() : "-"
        )
    );
  }

  private static String buildReportHtml(Audit a, List<ChecklistItem> items, List<AuditItemResult> results, BigDecimal score) {
    StringBuilder sb = new StringBuilder();
    sb.append("<html><body style='font-family:Arial,sans-serif'>");
    sb.append("<h2>Denetim Raporu</h2>");
    sb.append("<p><b>Başlık:</b> ").append(escape(a.getTitle())).append("</p>");
    sb.append("<p><b>Tip:</b> ").append(a.getAuditType().name()).append("</p>");
    sb.append("<p><b>Puan:</b> ").append(score.toPlainString()).append("</p>");
    if (a.getSummary() != null) {
      sb.append("<p><b>Özet:</b> ").append(escape(a.getSummary())).append("</p>");
    }
    sb.append("<hr/>");
    sb.append("<table cellpadding='6' cellspacing='0' style='border-collapse:collapse;width:100%'>");
    sb.append("<tr><th style='border:1px solid #ddd'>No</th><th style='border:1px solid #ddd'>Soru</th><th style='border:1px solid #ddd'>Skor</th><th style='border:1px solid #ddd'>Max</th><th style='border:1px solid #ddd'>Not</th></tr>");
    for (ChecklistItem ci : items) {
      var r = results.stream().filter(x -> x.getChecklistItem().getId().equals(ci.getId())).findFirst().orElse(null);
      sb.append("<tr>");
      sb.append("<td style='border:1px solid #ddd'>").append(ci.getItemNo()).append("</td>");
      sb.append("<td style='border:1px solid #ddd'>").append(escape(ci.getQuestion())).append("</td>");
      String scoreCell = r == null ? "0" : (!r.isApplicable() ? "N/A" : r.getScore().toPlainString());
      sb.append("<td style='border:1px solid #ddd'>").append(scoreCell).append("</td>");
      sb.append("<td style='border:1px solid #ddd'>").append(ci.getMaxScore().toPlainString()).append("</td>");
      sb.append("<td style='border:1px solid #ddd'>").append(r != null && r.getNote() != null ? escape(r.getNote()) : "").append("</td>");
      sb.append("</tr>");
    }
    sb.append("</table>");
    sb.append("</body></html>");
    return sb.toString();
  }

  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private AuditResponse toDto(Audit a) {
    var participants = participantRepo.findByAuditId(a.getId()).stream()
        .map(p -> new AuditResponse.Participant(
            p.getEmployee() != null ? p.getEmployee().getId() : null,
            p.getEmployee() != null ? p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName() : null,
            p.getRole()
        ))
        .toList();
    return new AuditResponse(
        a.getId(),
        a.getProject() != null ? a.getProject().getId() : null,
        a.getProject() != null ? a.getProject().getName() : null,
        a.getAuditType(),
        a.getChecklist() != null ? a.getChecklist().getId() : null,
        a.getChecklist() != null ? a.getChecklist().getTitle() : null,
        a.getTitle(),
        a.getSummary(),
        a.getStatus(),
        a.getStartedAt(),
        a.getFinishedAt(),
        a.getCalculatedScore(),
        a.getReportHtml(),
        participants
    );
  }
}


