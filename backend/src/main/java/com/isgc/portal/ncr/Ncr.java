package com.isgc.portal.ncr;

import com.isgc.portal.employee.Employee;
import com.isgc.portal.project.Project;
import com.isgc.portal.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "ncr")
@EntityListeners(AuditingEntityListener.class)
public class Ncr {
  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(name = "ncr_number", nullable = false, unique = true, length = 80)
  private String ncrNumber;

  @Column(name = "ncr_date", nullable = false)
  private LocalDate ncrDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;

  @Column(name = "responsible_organization", length = 255)
  private String responsibleOrganization;

  @Column(length = 255)
  private String location;

  @Column(length = 255)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @Column(name = "evidence_references", columnDefinition = "text")
  private String evidenceReferences;

  @Column(name = "proposed_corrective_action", columnDefinition = "text")
  private String proposedCorrectiveAction;

  @Column(name = "executed_corrective_action", columnDefinition = "text")
  private String executedCorrectiveAction;

  @Column(name = "target_completion_date")
  private LocalDate targetCompletionDate;

  @Column(name = "completion_date")
  private LocalDate completionDate;

  @Column(name = "root_cause_categories", nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String rootCauseCategories = "[]";

  @Column(length = 80)
  private String classification;

  @Column(name = "root_cause", columnDefinition = "text")
  private String rootCause;

  @Column(name = "corrective_action", columnDefinition = "text")
  private String correctiveAction;

  @Column(name = "preventive_action", columnDefinition = "text")
  private String preventiveAction;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "responsible_employee_id")
  private Employee responsibleEmployee;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @Column(name = "closed_date")
  private LocalDate closedDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NcrStatus status = NcrStatus.OPEN;

  @Column(name = "initiated_by", length = 200)
  private String initiatedBy;

  @Column(name = "approved_by", length = 200)
  private String approvedBy;

  @Column(name = "verified_by", length = 200)
  private String verifiedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "verification_status", length = 30)
  private NcrVerificationStatus verificationStatus;

  @Column(name = "iso_standards", nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String isoStandards = "[]";

  @Column(name = "followup_required", nullable = false)
  private boolean followupRequired;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String data = "{}";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_by_user_id")
  private User assignedBy;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public String getNcrNumber() { return ncrNumber; }
  public void setNcrNumber(String ncrNumber) { this.ncrNumber = ncrNumber; }
  public LocalDate getNcrDate() { return ncrDate; }
  public void setNcrDate(LocalDate ncrDate) { this.ncrDate = ncrDate; }
  public Project getProject() { return project; }
  public void setProject(Project project) { this.project = project; }
  public String getResponsibleOrganization() { return responsibleOrganization; }
  public void setResponsibleOrganization(String responsibleOrganization) { this.responsibleOrganization = responsibleOrganization; }
  public String getLocation() { return location; }
  public void setLocation(String location) { this.location = location; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getEvidenceReferences() { return evidenceReferences; }
  public void setEvidenceReferences(String evidenceReferences) { this.evidenceReferences = evidenceReferences; }
  public String getProposedCorrectiveAction() { return proposedCorrectiveAction; }
  public void setProposedCorrectiveAction(String proposedCorrectiveAction) { this.proposedCorrectiveAction = proposedCorrectiveAction; }
  public String getExecutedCorrectiveAction() { return executedCorrectiveAction; }
  public void setExecutedCorrectiveAction(String executedCorrectiveAction) { this.executedCorrectiveAction = executedCorrectiveAction; }
  public LocalDate getTargetCompletionDate() { return targetCompletionDate; }
  public void setTargetCompletionDate(LocalDate targetCompletionDate) { this.targetCompletionDate = targetCompletionDate; }
  public LocalDate getCompletionDate() { return completionDate; }
  public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }
  public String getRootCauseCategories() { return rootCauseCategories; }
  public void setRootCauseCategories(String rootCauseCategories) { this.rootCauseCategories = rootCauseCategories; }
  public String getClassification() { return classification; }
  public void setClassification(String classification) { this.classification = classification; }
  public String getRootCause() { return rootCause; }
  public void setRootCause(String rootCause) { this.rootCause = rootCause; }
  public String getCorrectiveAction() { return correctiveAction; }
  public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }
  public String getPreventiveAction() { return preventiveAction; }
  public void setPreventiveAction(String preventiveAction) { this.preventiveAction = preventiveAction; }
  public String getInitiatedBy() { return initiatedBy; }
  public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }
  public String getApprovedBy() { return approvedBy; }
  public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
  public String getVerifiedBy() { return verifiedBy; }
  public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
  public NcrVerificationStatus getVerificationStatus() { return verificationStatus; }
  public void setVerificationStatus(NcrVerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
  public String getIsoStandards() { return isoStandards; }
  public void setIsoStandards(String isoStandards) { this.isoStandards = isoStandards; }
  public boolean isFollowupRequired() { return followupRequired; }
  public void setFollowupRequired(boolean followupRequired) { this.followupRequired = followupRequired; }
  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }
  public Employee getResponsibleEmployee() { return responsibleEmployee; }
  public void setResponsibleEmployee(Employee responsibleEmployee) { this.responsibleEmployee = responsibleEmployee; }
  public LocalDate getDueDate() { return dueDate; }
  public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
  public LocalDate getClosedDate() { return closedDate; }
  public void setClosedDate(LocalDate closedDate) { this.closedDate = closedDate; }
  public NcrStatus getStatus() { return status; }
  public void setStatus(NcrStatus status) { this.status = status; }
  public String getData() { return data; }
  public void setData(String data) { this.data = data; }
  public User getAssignedBy() { return assignedBy; }
  public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
