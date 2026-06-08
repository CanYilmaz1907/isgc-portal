package com.isgc.portal.audit;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class AuditComplianceUtil {
  private AuditComplianceUtil() {}

  public static BigDecimal compliancePercent(BigDecimal score, BigDecimal maxScore) {
    if (maxScore == null || maxScore.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal s = score != null ? score : BigDecimal.ZERO;
    return s.multiply(BigDecimal.valueOf(100)).divide(maxScore, 2, RoundingMode.HALF_UP);
  }

  /** 0-49 red, 50-64 orange, 65-79 yellow, 80-89 light-green, 90-100 green */
  public static String colorZone(BigDecimal percent) {
    if (percent == null) return "red";
    int p = percent.intValue();
    if (p >= 90) return "green";
    if (p >= 80) return "light-green";
    if (p >= 65) return "yellow";
    if (p >= 50) return "orange";
    return "red";
  }

  public static String colorZoneLabel(String zone) {
    return switch (zone) {
      case "green" -> "Mükemmel (90-100%)";
      case "light-green" -> "İyi (80-89%)";
      case "yellow" -> "Orta (65-79%)";
      case "orange" -> "Zayıf (50-64%)";
      default -> "Kritik (0-49%)";
    };
  }

  public static int categoryGroup(int itemNo, int categoryNo) {
    return categoryNo > 0 ? categoryNo : itemNo;
  }
}
