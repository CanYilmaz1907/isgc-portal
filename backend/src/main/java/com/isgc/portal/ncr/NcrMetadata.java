package com.isgc.portal.ncr;

import java.util.Arrays;
import java.util.List;

public final class NcrMetadata {
  private NcrMetadata() {}

  public record Option(String value, String label) {}

  public static List<Option> rootCauseCategories() {
    return List.of(
        opt("UNAPPROVED_MATERIAL", "Onaysız / Uygunsuz Malzeme Kullanımı"),
        opt("MATERIAL_DAMAGE", "Malzeme Hasarı"),
        opt("IMPROPER_PLANNING", "Hatalı Planlama"),
        opt("DESIGN_DEVIATION", "Tasarımdan Sapma"),
        opt("INJURY", "Yaralanma / Kaza"),
        opt("IMS_DEVIATION", "Entegre Yönetim Sisteminden Sapma"),
        opt("SPEC_DEVIATION", "Spesifikasyon / Prosedürden Sapma"),
        opt("UNAPPROVED_DOCUMENT", "Onaysız Doküman Kullanımı"),
        opt("TRAFFIC_ACCIDENT", "Trafik / Araç Kazası"),
        opt("MISCELLANEOUS", "Diğer")
    );
  }

  public static List<Option> isoStandards() {
    return List.of(
        opt("ISO_9001_2015", "ISO 9001:2015 (Kalite Yönetim Sistemi)"),
        opt("ISO_14001_2015", "ISO 14001:2015 (Çevre Yönetim Sistemi)"),
        opt("ISO_45001_2018", "ISO 45001:2018 (İSG Yönetim Sistemi)")
    );
  }

  public static List<Option> statuses() {
    return Arrays.stream(NcrStatus.values())
        .map(s -> opt(s.name(), statusLabel(s)))
        .toList();
  }

  public static List<Option> verificationStatuses() {
    return Arrays.stream(NcrVerificationStatus.values())
        .map(s -> opt(s.name(), verificationLabel(s)))
        .toList();
  }

  private static Option opt(String value, String label) {
    return new Option(value, label);
  }

  private static String statusLabel(NcrStatus s) {
    return switch (s) {
      case OPEN -> "Açık";
      case CORRECTIVE_ACTION_PENDING -> "Düzeltici Faaliyet Bekleniyor";
      case VERIFICATION_PENDING -> "Doğrulama Bekliyor";
      case CLOSED -> "Kapalı";
      case REJECTED -> "Kabul Edilmedi";
    };
  }

  private static String verificationLabel(NcrVerificationStatus s) {
    return switch (s) {
      case PENDING -> "Beklemede";
      case APPROVED -> "Onaylandı";
      case REJECTED -> "Reddedildi";
    };
  }
}
