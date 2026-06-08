package com.isgc.portal.discipline;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DisciplineViolationTypes {
  private DisciplineViolationTypes() {}

  public record ViolationTypeOption(String code, String labelTr) {}

  private static final Map<DisciplineCategory, List<ViolationTypeOption>> BY_CATEGORY = new LinkedHashMap<>();

  static {
    BY_CATEGORY.put(DisciplineCategory.CAT_0, List.of(
        opt("CAT0_HIGH_ALT_UNSAFE", "Yüksekte Uygunsuz Çalışma (Güvenlik Ekipmanları Olmadan, Yüksekte İhmalkar, Tehlikeli Çalışma)"),
        opt("CAT0_FIGHT_INSULT", "Kavgaya Karışma / Hakaret / Küfür"),
        opt("CAT0_DISOBEY_SUPERVISOR", "Amirine Karşı Gelme (Yasal Emirlere Uymama, İSG Talimatlarını İhlal)"),
        opt("CAT0_DAMAGE_PROPERTY", "Şirket Malına Kasten Zarar Vermek"),
        opt("CAT0_UNAUTHORIZED_EQUIP", "Yetkisi Dışında Ekipman Kullanmak (Manlift, TIR, Kamyon, Elektrik Panosu vb.)"),
        opt("CAT0_UNSAFE_SUPERVISION", "Emri Altındakileri Kasten Uygunsuz Çalıştırmak"),
        opt("CAT0_SCAFFOLD_INTERFERE", "Yetkisi Dışında İskeleye Müdahale Etmek"),
        opt("CAT0_FIRE_SAFETY", "Yangın Güvenliği Kurallarının Ağır İhlali (Yanıcı/Patlayıcı Yakınında Ateş Yakmak, İzinsiz Sıcak Çalışma)"),
        opt("CAT0_PROVOCATION", "Provokasyon / İzinsiz Toplantı / Çalışanları Kışkırtmak"),
        opt("CAT0_ALCOHOL_DRUGS", "Alkol/Uyuşturucu Etkisinde İşe Çıkmak veya Şantiyeye Sokmak"),
        opt("CAT0_THEFT", "Hırsızlık / Gasp / Zimmete Para Geçirme"),
        opt("CAT0_FIRE_EQUIP_DAMAGE", "Yangın Ekipmanlarına Zarar Vermek"),
        opt("CAT0_SPEEDING", "Hız Sınırını Aşmak")
    ));
    BY_CATEGORY.put(DisciplineCategory.CAT_1, List.of(
        opt("CAT1_FIRE_PRECAUTION", "Yangın Güvenlik Önlemlerini Almamak"),
        opt("CAT1_ENDANGER_OTHERS", "Diğer Çalışanları Tehlikeye Atmak"),
        opt("CAT1_COMPANY_RULE", "Şirket Kuralı İhlali (Kameraya Müdahale, Araç Flaşörü Kullanmamak, Konteyneri Amacı Dışında Kullanmak, Fabrika Kapısını 10 dk.dan Fazla Açık Tutmak vb.)"),
        opt("CAT1_UNSAFE_METHOD", "Güvensiz Yapım Metodu"),
        opt("CAT1_NO_PERMIT", "İş İzni Olmadan Çalışma"),
        opt("CAT1_NO_PPE", "KKD Kullanmama (Gözlük, Çenebağı, Emniyet Kemeri, Siperlik)")
    ));
    BY_CATEGORY.put(DisciplineCategory.CAT_2, List.of(
        opt("CAT2_SLEEPING", "Sahada Uyumak"),
        opt("CAT2_WRONG_PATH", "Uygun Olmayan Alanlardan Geçmek/Yürümek"),
        opt("CAT2_UNDER_LOAD", "Kaldırılan Yükün Altına Girmek"),
        opt("CAT2_UNMAINTAINED_EQUIP", "Bakımsız Ekipman Kullanmak"),
        opt("CAT2_DANGEROUS_BEHAVIOR", "Tehlikeli Davranış"),
        opt("CAT2_EXCAVATION", "Yetersiz Kazı Güvenliği"),
        opt("CAT2_RESTRICTED_AREA", "Yasaklanmış/Tehlikeli Bölgeye İzinsiz Girmek"),
        opt("CAT2_PHONE_RED_ZONE", "Tehlikeli Alanda Telefon/Müzik/Kulaklık Kullanmak (Kırmızı Bölgede)")
    ));
    BY_CATEGORY.put(DisciplineCategory.CAT_3, List.of(
        opt("CAT3_IGNORE_WARNING", "Uyarı/İkaz/İşaretlemeye Uymamak"),
        opt("CAT3_HOUSEKEEPING", "Temizlik ve Tertip Düzenine Uymamak"),
        opt("CAT3_SMOKING", "Belirlenmiş Alan Dışında Sigara İçmek"),
        opt("CAT3_CAMP_RULES", "Kamp Kurallarına Uymamak")
    ));
  }

  private static ViolationTypeOption opt(String code, String labelTr) {
    return new ViolationTypeOption(code, labelTr);
  }

  public static Map<DisciplineCategory, List<ViolationTypeOption>> all() {
    return BY_CATEGORY;
  }

  public static List<ViolationTypeOption> forCategory(DisciplineCategory category) {
    return BY_CATEGORY.getOrDefault(category, List.of());
  }

  public static boolean isValidForCategory(DisciplineCategory category, String violationTypeCode) {
    if (violationTypeCode == null || violationTypeCode.isBlank()) return false;
    return forCategory(category).stream().anyMatch(v -> v.code().equals(violationTypeCode));
  }

  public static String labelFor(String code) {
    if (code == null) return null;
    for (var entry : BY_CATEGORY.entrySet()) {
      for (var opt : entry.getValue()) {
        if (opt.code().equals(code)) return opt.labelTr();
      }
    }
    return code;
  }
}
