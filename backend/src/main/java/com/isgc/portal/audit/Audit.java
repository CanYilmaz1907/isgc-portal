package com.isgc.portal.audit;

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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "audits")
@EntityListeners(AuditingEntityListener.class)
public class Audit {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;

  @Enumerated(EnumType.STRING)
  @Column(name = "audit_type", nullable = false, length = 30)
  private AuditType auditType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "checklist_id")
  private Checklist checklist;

  @Column(nullable = false, length = 300)
  private String title;

  @Column(columnDefinition = "text")
  private String summary;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AuditStatus status = AuditStatus.DRAFT;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(name = "calculated_score", precision = 8, scale = 2)
  private BigDecimal calculatedScore;

  @Column(name = "report_html", columnDefinition = "text")
  private String reportHtml;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id")
  private User createdBy;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public AuditType getAuditType() {
    return auditType;
  }

  public void setAuditType(AuditType auditType) {
    this.auditType = auditType;
  }

  public Checklist getChecklist() {
    return checklist;
  }

  public void setChecklist(Checklist checklist) {
    this.checklist = checklist;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public AuditStatus getStatus() {
    return status;
  }

  public void setStatus(AuditStatus status) {
    this.status = status;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(Instant finishedAt) {
    this.finishedAt = finishedAt;
  }

  public BigDecimal getCalculatedScore() {
    return calculatedScore;
  }

  public void setCalculatedScore(BigDecimal calculatedScore) {
    this.calculatedScore = calculatedScore;
  }

  public String getReportHtml() {
    return reportHtml;
  }

  public void setReportHtml(String reportHtml) {
    this.reportHtml = reportHtml;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }
}


