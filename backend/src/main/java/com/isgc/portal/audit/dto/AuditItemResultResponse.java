package com.isgc.portal.audit.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AuditItemResultResponse(
    UUID checklistItemId,
    int itemNo,
    int categoryNo,
    String question,
    BigDecimal score,
    BigDecimal maxScore,
    boolean applicable,
    String note
) {}
