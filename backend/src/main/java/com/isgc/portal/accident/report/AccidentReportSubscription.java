package com.isgc.portal.accident.report;

import com.isgc.portal.project.Project;
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
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "accident_report_subscriptions")
@EntityListeners(AuditingEntityListener.class)
public class AccidentReportSubscription {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;

  @Column(nullable = false)
  private boolean enabled = true;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReportFrequency frequency = ReportFrequency.WEEKLY;

  @Column(name = "hour_of_day", nullable = false)
  private int hourOfDay = 9;

  @Column(name = "minute_of_hour", nullable = false)
  private int minuteOfHour = 0;

  @Column(name = "to_emails", nullable = false, columnDefinition = "text")
  private String toEmails;

  @Column(name = "cc_emails", columnDefinition = "text")
  private String ccEmails;

  @Column(nullable = false, columnDefinition = "jsonb")
  private String filters = "{}";

  @Column(name = "last_sent_at")
  private Instant lastSentAt;

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

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public ReportFrequency getFrequency() {
    return frequency;
  }

  public void setFrequency(ReportFrequency frequency) {
    this.frequency = frequency;
  }

  public int getHourOfDay() {
    return hourOfDay;
  }

  public void setHourOfDay(int hourOfDay) {
    this.hourOfDay = hourOfDay;
  }

  public int getMinuteOfHour() {
    return minuteOfHour;
  }

  public void setMinuteOfHour(int minuteOfHour) {
    this.minuteOfHour = minuteOfHour;
  }

  public String getToEmails() {
    return toEmails;
  }

  public void setToEmails(String toEmails) {
    this.toEmails = toEmails;
  }

  public String getCcEmails() {
    return ccEmails;
  }

  public void setCcEmails(String ccEmails) {
    this.ccEmails = ccEmails;
  }

  public String getFilters() {
    return filters;
  }

  public void setFilters(String filters) {
    this.filters = filters;
  }

  public Instant getLastSentAt() {
    return lastSentAt;
  }

  public void setLastSentAt(Instant lastSentAt) {
    this.lastSentAt = lastSentAt;
  }
}


