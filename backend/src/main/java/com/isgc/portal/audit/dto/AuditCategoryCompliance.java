package com.isgc.portal.audit.dto;

import java.math.BigDecimal;

public record AuditCategoryCompliance(
    int categoryNo,
    String label,
    BigDecimal compliancePercent,
    String colorZone,
    int itemCount,
    int applicableCount
) {}
