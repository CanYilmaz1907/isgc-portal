package com.isgc.portal.accident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "accident_lookup_options")
public class AccidentLookupOption {
  @Id
  private UUID id;

  @Column(name = "option_type", nullable = false, length = 40)
  private String optionType;

  @Column(name = "option_code", nullable = false, length = 80)
  private String optionCode;

  @Column(name = "option_label", nullable = false)
  private String optionLabel;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(nullable = false)
  private boolean enabled = true;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getOptionType() {
    return optionType;
  }

  public void setOptionType(String optionType) {
    this.optionType = optionType;
  }

  public String getOptionCode() {
    return optionCode;
  }

  public void setOptionCode(String optionCode) {
    this.optionCode = optionCode;
  }

  public String getOptionLabel() {
    return optionLabel;
  }

  public void setOptionLabel(String optionLabel) {
    this.optionLabel = optionLabel;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
