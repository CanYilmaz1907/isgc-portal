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
@Table(name = "checklist_items")
@EntityListeners(AuditingEntityListener.class)
public class ChecklistItem {
  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "checklist_id", nullable = false)
  private Checklist checklist;

  @Column(name = "item_no", nullable = false)
  private int itemNo;

  @Column(name = "category_no")
  private Integer categoryNo;

  @Column(columnDefinition = "text", nullable = false)
  private String question;

  @Column(nullable = false, precision = 8, scale = 2)
  private BigDecimal weight = BigDecimal.ONE;

  @Column(name = "max_score", nullable = false, precision = 8, scale = 2)
  private BigDecimal maxScore = BigDecimal.ONE;

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

  public Checklist getChecklist() {
    return checklist;
  }

  public void setChecklist(Checklist checklist) {
    this.checklist = checklist;
  }

  public int getItemNo() {
    return itemNo;
  }

  public void setItemNo(int itemNo) {
    this.itemNo = itemNo;
  }

  public int getCategoryNo() {
    return ChecklistCategoryUtil.resolveCategoryNo(itemNo, categoryNo);
  }

  public void setCategoryNo(Integer categoryNo) {
    this.categoryNo = categoryNo;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public BigDecimal getWeight() {
    return weight;
  }

  public void setWeight(BigDecimal weight) {
    this.weight = weight;
  }

  public BigDecimal getMaxScore() {
    return maxScore;
  }

  public void setMaxScore(BigDecimal maxScore) {
    this.maxScore = maxScore;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
