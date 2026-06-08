package com.isgc.portal.accident;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "accidents")
@EntityListeners(AuditingEntityListener.class)
public class Accident {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "accident_type_id", nullable = false)
  private AccidentType accidentType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reported_by_user_id")
  private User reportedBy;

  @Column(name = "occurred_at")
  private Instant occurredAt;

  @Column
  private String location;

  @Enumerated(EnumType.STRING)
  @Column(name = "accident_class", nullable = false, length = 50)
  private AccidentClass accidentClass;

  @Enumerated(EnumType.STRING)
  @Column(name = "potential_level", nullable = false, length = 50)
  private PotentialLevel potentialLevel;

  @Column(columnDefinition = "text")
  private String description;

  @Column(name = "form_data", nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String formData = "{}";

  @Column(name = "root_cause_data", nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String rootCauseData = "{}";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AccidentStatus status = AccidentStatus.OPEN;

  // Excel-based fields
  @Column(length = 50)
  private String area; // Saha İçinde / Saha Dışında

  @Column(name = "hazard_source", length = 255)
  private String hazardSource; // Tehlike Kaynağı

  @Column(name = "injured_body_part", length = 255)
  private String injuredBodyPart; // Yaralanan Vücut Bölgesi

  @Column(name = "injury_type", length = 255)
  private String injuryType; // Yaralanma Türü

  @Column(name = "employee_registration_no", length = 50)
  private String employeeRegistrationNo; // Sicil Numarası

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supervisor_employee_id")
  private Employee supervisorEmployee; // İşin Süpervizörü

  @Column(name = "time_period", length = 50)
  private String timePeriod; // Saat (0800*1200 formatı)

  // New report template fields
  @Column(name = "group_company_name", length = 255)
  private String groupCompanyName; // Grup Şirket Adı

  @Column(name = "responsible_person", length = 255)
  private String responsiblePerson; // Sorumlu

  @Column(name = "estimated_cost", length = 100)
  private String estimatedCost; // Tahmini Maliyet

  @Column(name = "work_related")
  private Boolean workRelated = true; // İşle İlgili / İşle İlgisiz

  @Column(name = "work_during_accident", length = 500)
  private String workDuringAccident; // Kaza/Olay Esnasında Yapılan İş

  @Column(name = "injured_person_age")
  private Integer injuredPersonAge; // Kazazedenin Yaşı

  @Column(name = "injured_person_profession", length = 255)
  private String injuredPersonProfession; // Meslek

  @Column(name = "injured_person_gender", length = 20)
  private String injuredPersonGender; // Cinsiyet

  @Column(name = "injured_person_nationality", length = 100)
  private String injuredPersonNationality; // Milliyet

  @Column(name = "injured_person_company", length = 255)
  private String injuredPersonCompany; // Çalıştığı Firma

  @Column(name = "actions_taken", nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String actionsTaken = "[]"; // Alınmış/Alınacak Aksiyonlar

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prepared_by_user_id")
  private User preparedBy; // Raporu Hazırlayan

  @Column(name = "prepared_at")
  private Instant preparedAt; // Hazırlanma Tarihi

  @Column(name = "incident_no")
  private Integer incidentNo;

  @Column(length = 80)
  private String classification;

  @Column(name = "person_name")
  private String personName;

  @Column(name = "duration_on_project", length = 120)
  private String durationOnProject;

  @Column(name = "duration_in_role", length = 120)
  private String durationInRole;

  @Column(name = "work_supervisor")
  private String workSupervisor;

  @Column(name = "emergency_notification_sent")
  private Boolean emergencyNotificationSent;

  @Column(name = "vehicle_plate", length = 50)
  private String vehiclePlate;

  @Column(name = "vehicle_type", length = 80)
  private String vehicleType;

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

  public AccidentType getAccidentType() {
    return accidentType;
  }

  public void setAccidentType(AccidentType accidentType) {
    this.accidentType = accidentType;
  }

  public User getReportedBy() {
    return reportedBy;
  }

  public void setReportedBy(User reportedBy) {
    this.reportedBy = reportedBy;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public void setOccurredAt(Instant occurredAt) {
    this.occurredAt = occurredAt;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public AccidentClass getAccidentClass() {
    return accidentClass;
  }

  public void setAccidentClass(AccidentClass accidentClass) {
    this.accidentClass = accidentClass;
  }

  public PotentialLevel getPotentialLevel() {
    return potentialLevel;
  }

  public void setPotentialLevel(PotentialLevel potentialLevel) {
    this.potentialLevel = potentialLevel;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFormData() {
    return formData;
  }

  public void setFormData(String formData) {
    this.formData = formData;
  }

  public String getRootCauseData() {
    return rootCauseData;
  }

  public void setRootCauseData(String rootCauseData) {
    this.rootCauseData = rootCauseData;
  }

  public AccidentStatus getStatus() {
    return status;
  }

  public void setStatus(AccidentStatus status) {
    this.status = status;
  }

  public String getArea() {
    return area;
  }

  public void setArea(String area) {
    this.area = area;
  }

  public String getHazardSource() {
    return hazardSource;
  }

  public void setHazardSource(String hazardSource) {
    this.hazardSource = hazardSource;
  }

  public String getInjuredBodyPart() {
    return injuredBodyPart;
  }

  public void setInjuredBodyPart(String injuredBodyPart) {
    this.injuredBodyPart = injuredBodyPart;
  }

  public String getInjuryType() {
    return injuryType;
  }

  public void setInjuryType(String injuryType) {
    this.injuryType = injuryType;
  }

  public String getEmployeeRegistrationNo() {
    return employeeRegistrationNo;
  }

  public void setEmployeeRegistrationNo(String employeeRegistrationNo) {
    this.employeeRegistrationNo = employeeRegistrationNo;
  }

  public Employee getSupervisorEmployee() {
    return supervisorEmployee;
  }

  public void setSupervisorEmployee(Employee supervisorEmployee) {
    this.supervisorEmployee = supervisorEmployee;
  }

  public String getTimePeriod() {
    return timePeriod;
  }

  public void setTimePeriod(String timePeriod) {
    this.timePeriod = timePeriod;
  }

  public String getGroupCompanyName() {
    return groupCompanyName;
  }

  public void setGroupCompanyName(String groupCompanyName) {
    this.groupCompanyName = groupCompanyName;
  }

  public String getResponsiblePerson() {
    return responsiblePerson;
  }

  public void setResponsiblePerson(String responsiblePerson) {
    this.responsiblePerson = responsiblePerson;
  }

  public String getEstimatedCost() {
    return estimatedCost;
  }

  public void setEstimatedCost(String estimatedCost) {
    this.estimatedCost = estimatedCost;
  }

  public Boolean getWorkRelated() {
    return workRelated;
  }

  public void setWorkRelated(Boolean workRelated) {
    this.workRelated = workRelated;
  }

  public String getWorkDuringAccident() {
    return workDuringAccident;
  }

  public void setWorkDuringAccident(String workDuringAccident) {
    this.workDuringAccident = workDuringAccident;
  }

  public Integer getInjuredPersonAge() {
    return injuredPersonAge;
  }

  public void setInjuredPersonAge(Integer injuredPersonAge) {
    this.injuredPersonAge = injuredPersonAge;
  }

  public String getInjuredPersonProfession() {
    return injuredPersonProfession;
  }

  public void setInjuredPersonProfession(String injuredPersonProfession) {
    this.injuredPersonProfession = injuredPersonProfession;
  }

  public String getInjuredPersonGender() {
    return injuredPersonGender;
  }

  public void setInjuredPersonGender(String injuredPersonGender) {
    this.injuredPersonGender = injuredPersonGender;
  }

  public String getInjuredPersonNationality() {
    return injuredPersonNationality;
  }

  public void setInjuredPersonNationality(String injuredPersonNationality) {
    this.injuredPersonNationality = injuredPersonNationality;
  }

  public String getInjuredPersonCompany() {
    return injuredPersonCompany;
  }

  public void setInjuredPersonCompany(String injuredPersonCompany) {
    this.injuredPersonCompany = injuredPersonCompany;
  }

  public String getActionsTaken() {
    return actionsTaken;
  }

  public void setActionsTaken(String actionsTaken) {
    this.actionsTaken = actionsTaken;
  }

  public User getPreparedBy() {
    return preparedBy;
  }

  public void setPreparedBy(User preparedBy) {
    this.preparedBy = preparedBy;
  }

  public Instant getPreparedAt() {
    return preparedAt;
  }

  public void setPreparedAt(Instant preparedAt) {
    this.preparedAt = preparedAt;
  }

  public Integer getIncidentNo() {
    return incidentNo;
  }

  public void setIncidentNo(Integer incidentNo) {
    this.incidentNo = incidentNo;
  }

  public String getClassification() {
    return classification;
  }

  public void setClassification(String classification) {
    this.classification = classification;
  }

  public String getPersonName() {
    return personName;
  }

  public void setPersonName(String personName) {
    this.personName = personName;
  }

  public String getDurationOnProject() {
    return durationOnProject;
  }

  public void setDurationOnProject(String durationOnProject) {
    this.durationOnProject = durationOnProject;
  }

  public String getDurationInRole() {
    return durationInRole;
  }

  public void setDurationInRole(String durationInRole) {
    this.durationInRole = durationInRole;
  }

  public String getWorkSupervisor() {
    return workSupervisor;
  }

  public void setWorkSupervisor(String workSupervisor) {
    this.workSupervisor = workSupervisor;
  }

  public Boolean getEmergencyNotificationSent() {
    return emergencyNotificationSent;
  }

  public void setEmergencyNotificationSent(Boolean emergencyNotificationSent) {
    this.emergencyNotificationSent = emergencyNotificationSent;
  }

  public String getVehiclePlate() {
    return vehiclePlate;
  }

  public void setVehiclePlate(String vehiclePlate) {
    this.vehiclePlate = vehiclePlate;
  }

  public String getVehicleType() {
    return vehicleType;
  }

  public void setVehicleType(String vehicleType) {
    this.vehicleType = vehicleType;
  }
}


