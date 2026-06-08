package com.isgc.portal.audit.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AuditSubmitRequest(
    @NotNull List<ItemScore> items,
    String summary
) {
  public record ItemScore(
      @NotNull UUID checklistItemId,
      @NotNull BigDecimal score,
      Boolean applicable,
      String note
  ) {}
}


