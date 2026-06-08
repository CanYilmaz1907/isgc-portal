package com.isgc.portal.accident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accident_root_causes")
public class AccidentRootCause {
  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "accident_id", nullable = false)
  private Accident accident;

  @Column(name = "cause_code", nullable = false, length = 10)
  private String causeCode;

  @Column(name = "cause_label", nullable = false, length = 500)
  private String causeLabel;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Accident getAccident() {
    return accident;
  }

  public void setAccident(Accident accident) {
    this.accident = accident;
  }

  public String getCauseCode() {
    return causeCode;
  }

  public void setCauseCode(String causeCode) {
    this.causeCode = causeCode;
  }

  public String getCauseLabel() {
    return causeLabel;
  }

  public void setCauseLabel(String causeLabel) {
    this.causeLabel = causeLabel;
  }
}
