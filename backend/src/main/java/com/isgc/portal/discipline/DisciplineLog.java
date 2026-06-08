package com.isgc.portal.discipline;

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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "discipline_logs")
@EntityListeners(AuditingEntityListener.class)
public class DisciplineLog {
  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(name = "sequence_no")
  private Integer sequenceNo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "full_name", length = 200)
  private String fullName;

  @Column(name = "employee_registration_no", length = 80)
  private String employeeRegistrationNo;

  @Column(length = 200)
  private String company;

  @Column(name = "job_title", length = 150)
  private String jobTitle;

  @Column(name = "work_area", length = 200)
  private String workArea;

  @Enumerated(EnumType.STRING)
  @Column(name = "category_level", length = 30)
  private DisciplineCategory categoryLevel;

  @Column(length = 120)
  private String category;

  @Column(name = "violation_type", length = 255)
  private String violationType;

  @Column(name = "violation_description", columnDefinition = "text")
  private String violationDescription;

  @Column(columnDefinition = "text")
  private String description;

  @Column(nullable = false)
  private int severity = 1;

  @Column(length = 150)
  private String profession;

  @Column(name = "responsible_person", length = 200)
  private String responsiblePerson;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "violating_employee_id")
  private Employee violatingEmployee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "violating_manager_employee_id")
  private Employee violatingManagerEmployee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id")
  private User createdBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private DisciplineStatus status = DisciplineStatus.UYARI;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(name = "repeat_count", nullable = false)
  private int repeatCount;

  @Column(name = "penalty_amount", precision = 12, scale = 2)
  private BigDecimal penaltyAmount;

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

  public Integer getSequenceNo() {
    return sequenceNo;
  }

  public void setSequenceNo(Integer sequenceNo) {
    this.sequenceNo = sequenceNo;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public void setOccurredAt(Instant occurredAt) {
    this.occurredAt = occurredAt;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmployeeRegistrationNo() {
    return employeeRegistrationNo;
  }

  public void setEmployeeRegistrationNo(String employeeRegistrationNo) {
    this.employeeRegistrationNo = employeeRegistrationNo;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getWorkArea() {
    return workArea;
  }

  public void setWorkArea(String workArea) {
    this.workArea = workArea;
  }

  public DisciplineCategory getCategoryLevel() {
    return categoryLevel;
  }

  public void setCategoryLevel(DisciplineCategory categoryLevel) {
    this.categoryLevel = categoryLevel;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getViolationType() {
    return violationType;
  }

  public void setViolationType(String violationType) {
    this.violationType = violationType;
  }

  public String getViolationDescription() {
    return violationDescription;
  }

  public void setViolationDescription(String violationDescription) {
    this.violationDescription = violationDescription;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getSeverity() {
    return severity;
  }

  public void setSeverity(int severity) {
    this.severity = severity;
  }

  public String getProfession() {
    return profession;
  }

  public void setProfession(String profession) {
    this.profession = profession;
  }

  public String getResponsiblePerson() {
    return responsiblePerson;
  }

  public void setResponsiblePerson(String responsiblePerson) {
    this.responsiblePerson = responsiblePerson;
  }

  public Employee getViolatingEmployee() {
    return violatingEmployee;
  }

  public void setViolatingEmployee(Employee violatingEmployee) {
    this.violatingEmployee = violatingEmployee;
  }

  public Employee getViolatingManagerEmployee() {
    return violatingManagerEmployee;
  }

  public void setViolatingManagerEmployee(Employee violatingManagerEmployee) {
    this.violatingManagerEmployee = violatingManagerEmployee;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  public DisciplineStatus getStatus() {
    return status;
  }

  public void setStatus(DisciplineStatus status) {
    this.status = status;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public void setRepeatCount(int repeatCount) {
    this.repeatCount = repeatCount;
  }

  public BigDecimal getPenaltyAmount() {
    return penaltyAmount;
  }

  public void setPenaltyAmount(BigDecimal penaltyAmount) {
    this.penaltyAmount = penaltyAmount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
