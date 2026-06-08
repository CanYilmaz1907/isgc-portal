package com.isgc.portal.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@Table(name = "audit_item_results")
@EntityListeners(AuditingEntityListener.class)
public class AuditItemResult {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "audit_id", nullable = false)
  private Audit audit;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "checklist_item_id", nullable = false)
  private ChecklistItem checklistItem;

  @Column(nullable = false, precision = 8, scale = 2)
  private BigDecimal score;

  @Column(nullable = false)
  private boolean applicable = true;

  @Column(columnDefinition = "text")
  private String note;

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

  public Audit getAudit() {
    return audit;
  }

  public void setAudit(Audit audit) {
    this.audit = audit;
  }

  public ChecklistItem getChecklistItem() {
    return checklistItem;
  }

  public void setChecklistItem(ChecklistItem checklistItem) {
    this.checklistItem = checklistItem;
  }

  public BigDecimal getScore() {
    return score;
  }

  public void setScore(BigDecimal score) {
    this.score = score;
  }

  public boolean isApplicable() {
    return applicable;
  }

  public void setApplicable(boolean applicable) {
    this.applicable = applicable;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}


