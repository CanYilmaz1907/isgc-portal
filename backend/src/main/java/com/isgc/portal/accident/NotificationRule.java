package com.isgc.portal.accident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "notification_rules")
@EntityListeners(AuditingEntityListener.class)
public class NotificationRule {
  @Id
  @Column(nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "accident_class", nullable = false, length = 50)
  private AccidentClass accidentClass;

  @Enumerated(EnumType.STRING)
  @Column(name = "potential_level", nullable = false, length = 50)
  private PotentialLevel potentialLevel;

  @Column(name = "to_emails", nullable = false, columnDefinition = "text")
  private String toEmails;

  @Column(name = "cc_emails", columnDefinition = "text")
  private String ccEmails;

  @Column(nullable = false)
  private boolean enabled = true;

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

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}


