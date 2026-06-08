export type RootCauseData = {
  // Backend tarafında zaten `root_cause_data` jsonb içinde bu alan isimleri saklanıyor.
  // Spec'e uyum için:
  // - unsafeBehavior/unsafeCondition = Direkt Sebepler
  // - personalFactors/workFactors = Kök Sebepler
  unsafeBehavior: string[]
  unsafeCondition: string[]
  personalFactors: string[]
  workFactors: string[]
}

// Direkt Sebepler (Immediate Causes) — Spec'teki tam liste
export const DIRECT_CAUSES_PROCEDURES_OPTIONS = [
  '1-1: Bireysel ihlal',
  '1-2: Grup ihlali',
  '1-3: Süpervizör ihlali',
  '1-4: Yetkisi dışında ekipman kullanımı',
  '1-5: Hatalı duruş / pozisyon',
  '1-6: Fiziksel kapasitesinin üstünde çalışma',
  '1-7: Hızlı çalışma ya da hareket etme',
  '1-8: Hatalı yük kaldırma',
  '1-9: Kestirme yol kullanma',
  '1-10: Diğer'
] as const

export const DIRECT_CAUSES_EQUIPMENT_OPTIONS = [
  '2-1: Ekipmanın hatalı kullanımı',
  '2-2: Arızalı ekipman kullanma',
  '2-3: Uygun olmayan hızda ekipman kullanımı',
  '2-4: Aletin uygun olmayan yere konması',
  '2-5: Enerjili sistemin bakım/onarımı',
  '2-6: Diğer'
] as const

export const DIRECT_CAUSES_PROTECTION_MISSING_OPTIONS = [
  '3-1: Mevcut tehlikeleri bilmeme',
  '3-2: KKE kullanmama',
  '3-3: Hatalı KKE kullanma',
  '3-4: Enerji dolu hatta çalışma',
  '3-5: Sabitlenmemiş malzeme/ekipman',
  '3-6: Hasarlı muhafaza/uyarı sistemi kullanımı',
  '3-7: Güvenlik cihazını devre dışı bırakma',
  '3-8: KKE mevcut değil',
  '3-9: Diğer'
] as const

export const DIRECT_CAUSES_ATTENTION_OPTIONS = [
  '4-1: Yanlış karar verme / riski değerlendirememe',
  '4-2: Konsantrasyon kaybı',
  '4-3: Yürürken adımına dikkat etmeme',
  '4-4: Şakalaşma',
  '4-5: Dikkatini dağıtacak unsurlar'
] as const

export const DIRECT_CAUSES_PROTECTION_SYSTEMS_OPTIONS = [
  '5-1: Eksik/yetersiz koruma',
  '5-2: Koruma sisteminin kaldırılmış olması',
  '5-3: Yanlış boyutta/tipte ekipman',
  '5-4: Güvenlik cihazı arızası',
  '5-5: Uygun olmayan uyarı sistemi'
] as const

export const DIRECT_CAUSES_TOOLS_EQUIPMENT_OPTIONS = [
  '6-1: Aşınmış/yıpranmış ekipman',
  '6-2: Tasarım/yapım hatası',
  '6-3: Bakım yetersizliği',
  '6-4: Yanlış araç seçimi'
] as const

export const DIRECT_CAUSES_WORKPLACE_OPTIONS = [
  '7-1: Yetersiz aydınlatma',
  '7-2: Yetersiz havalandırma',
  '7-3: Aşırı gürültü',
  '7-4: Kaygan zemin',
  '7-5: Düzensiz/dağınık çalışma alanı',
  '7-6: Yüksekte güvensiz çalışma yüzeyi',
  '7-7: Hatalı depolama',
  '7-8: Engel/çıkıntı',
  '7-9: Uygun olmayan sıcaklık',
  '7-10: Dar/kısıtlı alan',
  '7-11: Yürüme yollarının kaygan olması'
] as const

// unsafeBehavior/unsafeCondition alan isimleri korunuyor:
// - unsafeBehavior: Direkt Sebepler (1,2,3,4,6)
// - unsafeCondition: Direkt Sebepler (5,7)
export const UNSAFE_BEHAVIOR_OPTIONS = [
  ...DIRECT_CAUSES_PROCEDURES_OPTIONS,
  ...DIRECT_CAUSES_EQUIPMENT_OPTIONS,
  ...DIRECT_CAUSES_PROTECTION_MISSING_OPTIONS,
  ...DIRECT_CAUSES_ATTENTION_OPTIONS,
  ...DIRECT_CAUSES_TOOLS_EQUIPMENT_OPTIONS
] as const

export const UNSAFE_CONDITION_OPTIONS = [
  ...DIRECT_CAUSES_PROTECTION_SYSTEMS_OPTIONS,
  ...DIRECT_CAUSES_WORKPLACE_OPTIONS
] as const

// Kök Sebepler (Root Causes) — Spec'teki tam liste
export const ROOT_CAUSES_SKILL_LEVEL_OPTIONS = [
  '1-1: Yetersiz başlangıç eğitimi',
  '1-2: Yetersiz uygulama/alıştırma eğitimi',
  '1-3: Talimat yetersizliği',
  '1-4: Becerilerin güncellenmemesi',
  '1-5: Yanlış anlama',
  '1-6: Becerilerin kontrol edilmemesi'
] as const

export const ROOT_CAUSES_MOTIVATION_OPTIONS = [
  '2-1: Yetersiz performans standardı',
  '2-2: Aşırı yorgunluk',
  '2-3: Yetersiz pekiştirme'
] as const

export const ROOT_CAUSES_PHYSICAL_CAPABILITY_OPTIONS = [
  '3-1: Yüksekliğe tolerans sorunu',
  '3-2: Görme/işitme bozukluğu',
  '3-3: Çalışanın acele etmesi gerektiğini düşünmesi',
  '3-4: Fiziksel kondisyon yetersizliği'
] as const

export const ROOT_CAUSES_PHYSICAL_INADEQUACY_OPTIONS = [
  '4-1: Yanlış karar verme/risk değerlendirememe',
  '4-2: Aşırı yorgunluk',
  '4-3: Becerilerin kontrol edilmemesi',
  '4-4: Alkol/uyuşturucu etkisi'
] as const

export const ROOT_CAUSES_JOB_PLANNING_OPTIONS = [
  '5-1: Düşük muhakeme yeteneği',
  '5-2: Yetersiz risk değerlendirmesi',
  '5-3: Uygunsuz iş planlaması',
  '5-4: Yanlış talimat/prosedür',
  '5-5: Gözetim eksikliği'
] as const

export const ROOT_CAUSES_LEADERSHIP_OPTIONS = [
  '6-1: Yetersiz denetim',
  '6-2: Yanlış yetkilendirme',
  '6-3: İletişim hatası'
] as const

export const ROOT_CAUSES_PURCHASING_OPTIONS = [
  '7-1: Uygun olmayan malzeme temini',
  '7-2: Yetersiz spesifikasyon'
] as const

export const ROOT_CAUSES_ENGINEERING_OPTIONS = [
  '8-1: Yetersiz tasarım',
  '8-2: Ergonomik olmayan çalışma düzeni',
  '8-3: Yetersiz mühendislik analizi'
] as const

export const ROOT_CAUSES_MAINTENANCE_OPTIONS = [
  '9-1: Yetersiz bakım programı',
  '9-2: Yanlış parça kullanımı'
] as const

export const ROOT_CAUSES_TOOLS_EQUIPMENT_ROOT_OPTIONS = [
  '10-1: Yetersiz temin',
  '10-2: Bakımsız ekipman',
  '10-3: Yanlış araç seçimi'
] as const

export const ROOT_CAUSES_STANDARDS_PROCEDURES_OPTIONS = [
  '11-1: Yetersiz prosedür',
  '11-2: Güncel olmayan prosedür',
  '11-3: Prosedüre uyumsuzluk'
] as const

export const ROOT_CAUSES_PERSONAL_FACTORS_OPTIONS = [
  '12-1: Yorgunluk',
  '12-2: Dikkat dağınıklığı',
  '12-3: Rehavet',
  '12-4: Motivasyon eksikliği'
] as const

export const ROOT_CAUSES_JOB_FACTORS_OPTIONS = [
  '13-1: Zaman baskısı',
  '13-2: Uygun olmayan iş ortamı',
  '13-3: Yetersiz kaynak'
] as const

// personalFactors/workFactors alan isimleri korunuyor:
// - personalFactors: 1..6
// - workFactors: 7..13
export const PERSONAL_FACTORS_OPTIONS = [
  ...ROOT_CAUSES_SKILL_LEVEL_OPTIONS,
  ...ROOT_CAUSES_MOTIVATION_OPTIONS,
  ...ROOT_CAUSES_PHYSICAL_CAPABILITY_OPTIONS,
  ...ROOT_CAUSES_PHYSICAL_INADEQUACY_OPTIONS,
  ...ROOT_CAUSES_JOB_PLANNING_OPTIONS,
  ...ROOT_CAUSES_LEADERSHIP_OPTIONS
] as const

export const WORK_FACTORS_OPTIONS = [
  ...ROOT_CAUSES_PURCHASING_OPTIONS,
  ...ROOT_CAUSES_ENGINEERING_OPTIONS,
  ...ROOT_CAUSES_MAINTENANCE_OPTIONS,
  ...ROOT_CAUSES_TOOLS_EQUIPMENT_ROOT_OPTIONS,
  ...ROOT_CAUSES_STANDARDS_PROCEDURES_OPTIONS,
  ...ROOT_CAUSES_PERSONAL_FACTORS_OPTIONS,
  ...ROOT_CAUSES_JOB_FACTORS_OPTIONS
] as const

export function emptyRootCause(): RootCauseData {
  return {
    unsafeBehavior: [],
    unsafeCondition: [],
    personalFactors: [],
    workFactors: []
  }
}

export function safeParseRootCause(json: string | null | undefined): RootCauseData {
  if (!json) return emptyRootCause()
  try {
    const obj = JSON.parse(json) as Partial<RootCauseData> & Record<string, unknown>
    // Legacy format support
    if (obj.human || obj.equipment || obj.environment || obj.management) {
      // Old format, return empty
      return emptyRootCause()
    }
    return {
      unsafeBehavior: Array.isArray(obj.unsafeBehavior) ? obj.unsafeBehavior : [],
      unsafeCondition: Array.isArray(obj.unsafeCondition) ? obj.unsafeCondition : [],
      personalFactors: Array.isArray(obj.personalFactors) ? obj.personalFactors : [],
      workFactors: Array.isArray(obj.workFactors) ? obj.workFactors : []
    }
  } catch {
    return emptyRootCause()
  }
}
