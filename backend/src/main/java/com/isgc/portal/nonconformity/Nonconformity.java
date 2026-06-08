package com.isgc.portal.nonconformity;

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
@Table(name = "nonconformities")
@EntityListeners(AuditingEntityListener.class)
public class Nonconformity {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", nullable = false)
  private NonconformityTemplate template;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hazard_class_id")
  private HazardClass hazardClass;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "responsible_employee_id")
  private Employee responsibleEmployee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_by_user_id")
  private User assignedBy;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NonconformityStatus status = NonconformityStatus.OPEN;

  @Column(length = 30)
  private String severity;

  @Column(nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String data = "{}";

  @Column(name = "last_reminded_at")
  private Instant lastRemindedAt;

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

  public NonconformityTemplate getTemplate() {
    return template;
  }

  public void setTemplate(NonconformityTemplate template) {
    this.template = template;
  }

  public HazardClass getHazardClass() {
    return hazardClass;
  }

  public void setHazardClass(HazardClass hazardClass) {
    this.hazardClass = hazardClass;
  }

  public Employee getResponsibleEmployee() {
    return responsibleEmployee;
  }

  public void setResponsibleEmployee(Employee responsibleEmployee) {
    this.responsibleEmployee = responsibleEmployee;
  }

  public User getAssignedBy() {
    return assignedBy;
  }

  public void setAssignedBy(User assignedBy) {
    this.assignedBy = assignedBy;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public NonconformityStatus getStatus() {
    return status;
  }

  public void setStatus(NonconformityStatus status) {
    this.status = status;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public Instant getLastRemindedAt() {
    return lastRemindedAt;
  }

  public void setLastRemindedAt(Instant lastRemindedAt) {
    this.lastRemindedAt = lastRemindedAt;
  }
}


