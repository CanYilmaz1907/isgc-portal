package com.isgc.portal.accident;

import java.util.HashMap;
import java.util.Map;

public class AccidentPdfTranslations {
  private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
  
  static {
    Map<String, String> tr = new HashMap<>();
    tr.put("title", "KAZA RAPORU");
    tr.put("date", "Tarih:");
    tr.put("location", "Konum:");
    tr.put("accidentType", "Kaza Tipi:");
    tr.put("accidentClass", "Kaza Sinifi:");
    tr.put("potentialLevel", "Potansiyel Seviye:");
    tr.put("status", "Durum:");
    tr.put("area", "Alan:");
    tr.put("hazardSource", "Tehlike Kaynagi:");
    tr.put("injuredBodyPart", "Yaralanan Vucut Bolgesi:");
    tr.put("injuryType", "Yaralanma Turu:");
    tr.put("employeeRegistrationNo", "Sicil No:");
    tr.put("supervisor", "Supervisor:");
    tr.put("time", "Saat:");
    tr.put("description", "Aciklama:");
    tr.put("injuredPeople", "YARALI KISILER");
    tr.put("keyPeople", "KILIT KISILER");
    tr.put("formData", "FORM VERILERI");
    tr.put("rootCauseAnalysis", "KOK NEDEN ANALIZI");
    tr.put("photosFiles", "EK FOTOGRAFLAR/DOSYALAR");
    tr.put("file", "Dosya:");
    tr.put("fileNotLoaded", " (yuklenemedi)");
    tr.put("yes", "Evet");
    tr.put("groupCompanyName", "Grup Şirket Adı:");
    tr.put("project", "Proje Adı:");
    tr.put("responsiblePerson", "Sorumlu:");
    tr.put("estimatedCost", "Tahmini Maliyet:");
    tr.put("injuredPersonName", "Adı Soyadı:");
    tr.put("injuredPersonAge", "Yaş:");
    tr.put("injuredPersonProfession", "Meslek:");
    tr.put("injuredPersonGender", "Cinsiyet:");
    tr.put("injuredPersonNationality", "Milliyet:");
    tr.put("injuredPersonCompany", "Çalıştığı Firma:");
    TRANSLATIONS.put("tr", tr);
    
    Map<String, String> en = new HashMap<>();
    en.put("title", "ACCIDENT REPORT");
    en.put("date", "Date:");
    en.put("location", "Location:");
    en.put("accidentType", "Accident Type:");
    en.put("accidentClass", "Accident Class:");
    en.put("potentialLevel", "Potential Level:");
    en.put("status", "Status:");
    en.put("area", "Area:");
    en.put("hazardSource", "Hazard Source:");
    en.put("injuredBodyPart", "Injured Body Part:");
    en.put("injuryType", "Injury Type:");
    en.put("employeeRegistrationNo", "Registration No:");
    en.put("supervisor", "Supervisor:");
    en.put("time", "Time:");
    en.put("description", "Description:");
    en.put("injuredPeople", "INJURED PEOPLE");
    en.put("keyPeople", "KEY PEOPLE");
    en.put("formData", "FORM DATA");
    en.put("rootCauseAnalysis", "ROOT CAUSE ANALYSIS");
    en.put("photosFiles", "ATTACHED PHOTOS/FILES");
    en.put("file", "File:");
    en.put("fileNotLoaded", " (not loaded)");
    en.put("yes", "Yes");
    en.put("groupCompanyName", "Group Company Name:");
    en.put("project", "Project Name:");
    en.put("responsiblePerson", "Responsible:");
    en.put("estimatedCost", "Estimated Cost:");
    en.put("injuredPersonName", "Full Name:");
    en.put("injuredPersonAge", "Age:");
    en.put("injuredPersonProfession", "Profession:");
    en.put("injuredPersonGender", "Gender:");
    en.put("injuredPersonNationality", "Nationality:");
    en.put("injuredPersonCompany", "Company:");
    TRANSLATIONS.put("en", en);
    
    Map<String, String> ru = new HashMap<>();
    ru.put("title", "ОТЧЕТ О НЕСЧАСТНОМ СЛУЧАЕ");
    ru.put("date", "Дата:");
    ru.put("location", "Место:");
    ru.put("accidentType", "Тип несчастного случая:");
    ru.put("accidentClass", "Класс несчастного случая:");
    ru.put("potentialLevel", "Уровень потенциальной опасности:");
    ru.put("status", "Статус:");
    ru.put("area", "Область:");
    ru.put("hazardSource", "Источник опасности:");
    ru.put("injuredBodyPart", "Пораженная часть тела:");
    ru.put("injuryType", "Тип травмы:");
    ru.put("employeeRegistrationNo", "Регистрационный номер:");
    ru.put("supervisor", "Супервайзер:");
    ru.put("time", "Время:");
    ru.put("description", "Описание:");
    ru.put("injuredPeople", "ПОСТРАДАВШИЕ");
    ru.put("keyPeople", "КЛЮЧЕВЫЕ ЛИЦА");
    ru.put("formData", "ДАННЫЕ ФОРМЫ");
    ru.put("rootCauseAnalysis", "АНАЛИЗ ПЕРВОПРИЧИН");
    ru.put("photosFiles", "ПРИЛОЖЕННЫЕ ФОТО/ФАЙЛЫ");
    ru.put("file", "Файл:");
    ru.put("fileNotLoaded", " (не загружен)");
    ru.put("yes", "Да");
    ru.put("groupCompanyName", "Название группы компаний:");
    ru.put("project", "Название проекта:");
    ru.put("responsiblePerson", "Ответственный:");
    ru.put("estimatedCost", "Предполагаемая стоимость:");
    ru.put("injuredPersonName", "ФИО:");
    ru.put("injuredPersonAge", "Возраст:");
    ru.put("injuredPersonProfession", "Профессия:");
    ru.put("injuredPersonGender", "Пол:");
    ru.put("injuredPersonNationality", "Национальность:");
    ru.put("injuredPersonCompany", "Компания:");
    TRANSLATIONS.put("ru", ru);
  }

  public static String translate(String lang, String key) {
    if (lang == null || lang.isEmpty()) lang = "tr";
    Map<String, String> langMap = TRANSLATIONS.getOrDefault(lang, TRANSLATIONS.get("tr"));
    return langMap.getOrDefault(key, key);
  }

  private static final Map<String, Map<String, String>> STATUS_TRANSLATIONS = new HashMap<>();
  private static final Map<String, Map<String, String>> CLASS_TRANSLATIONS = new HashMap<>();
  private static final Map<String, Map<String, String>> LEVEL_TRANSLATIONS = new HashMap<>();
  
  static {
    Map<String, String> trStatus = new HashMap<>();
    trStatus.put("OPEN", "ACIK");
    trStatus.put("CLOSED", "KAPALI");
    trStatus.put("IN_PROGRESS", "DEVAM EDIYOR");
    STATUS_TRANSLATIONS.put("tr", trStatus);
    
    Map<String, String> enStatus = new HashMap<>();
    enStatus.put("OPEN", "OPEN");
    enStatus.put("CLOSED", "CLOSED");
    enStatus.put("IN_PROGRESS", "IN PROGRESS");
    STATUS_TRANSLATIONS.put("en", enStatus);
    
    Map<String, String> ruStatus = new HashMap<>();
    ruStatus.put("OPEN", "ОТКРЫТО");
    ruStatus.put("CLOSED", "ЗАКРЫТО");
    ruStatus.put("IN_PROGRESS", "В ПРОЦЕССЕ");
    STATUS_TRANSLATIONS.put("ru", ruStatus);
    
    Map<String, String> trClass = new HashMap<>();
    trClass.put("CLASS_A", "SINIF A");
    trClass.put("CLASS_B", "SINIF B");
    trClass.put("CLASS_C", "SINIF C");
    CLASS_TRANSLATIONS.put("tr", trClass);
    
    Map<String, String> enClass = new HashMap<>();
    enClass.put("CLASS_A", "CLASS A");
    enClass.put("CLASS_B", "CLASS B");
    enClass.put("CLASS_C", "CLASS C");
    CLASS_TRANSLATIONS.put("en", enClass);
    
    Map<String, String> ruClass = new HashMap<>();
    ruClass.put("CLASS_A", "КЛАСС A");
    ruClass.put("CLASS_B", "КЛАСС B");
    ruClass.put("CLASS_C", "КЛАСС C");
    CLASS_TRANSLATIONS.put("ru", ruClass);
    
    Map<String, String> trLevel = new HashMap<>();
    trLevel.put("LOW", "DUSUK");
    trLevel.put("MEDIUM", "ORTA");
    trLevel.put("HIGH", "YUKSEK");
    trLevel.put("CRITICAL", "KRITIK");
    LEVEL_TRANSLATIONS.put("tr", trLevel);
    
    Map<String, String> enLevel = new HashMap<>();
    enLevel.put("LOW", "LOW");
    enLevel.put("MEDIUM", "MEDIUM");
    enLevel.put("HIGH", "HIGH");
    enLevel.put("CRITICAL", "CRITICAL");
    LEVEL_TRANSLATIONS.put("en", enLevel);
    
    Map<String, String> ruLevel = new HashMap<>();
    ruLevel.put("LOW", "НИЗКИЙ");
    ruLevel.put("MEDIUM", "СРЕДНИЙ");
    ruLevel.put("HIGH", "ВЫСОКИЙ");
    ruLevel.put("CRITICAL", "КРИТИЧЕСКИЙ");
    LEVEL_TRANSLATIONS.put("ru", ruLevel);
  }

  public static String translateStatus(String lang, String status) {
    if (lang == null || lang.isEmpty()) lang = "tr";
    if (status == null) return "-";
    Map<String, String> langMap = STATUS_TRANSLATIONS.getOrDefault(lang, STATUS_TRANSLATIONS.get("tr"));
    return langMap.getOrDefault(status, status);
  }

  public static String translateAccidentClass(String lang, String accidentClass) {
    if (lang == null || lang.isEmpty()) lang = "tr";
    if (accidentClass == null) return "-";
    Map<String, String> langMap = CLASS_TRANSLATIONS.getOrDefault(lang, CLASS_TRANSLATIONS.get("tr"));
    return langMap.getOrDefault(accidentClass, accidentClass);
  }

  public static String translatePotentialLevel(String lang, String potentialLevel) {
    if (lang == null || lang.isEmpty()) lang = "tr";
    if (potentialLevel == null) return "-";
    Map<String, String> langMap = LEVEL_TRANSLATIONS.getOrDefault(lang, LEVEL_TRANSLATIONS.get("tr"));
    return langMap.getOrDefault(potentialLevel, potentialLevel);
  }

  private static final Map<String, Map<String, String>> HAZARD_SOURCE_TRANSLATIONS = new HashMap<>();
  private static final Map<String, Map<String, String>> BODY_PART_TRANSLATIONS = new HashMap<>();
  private static final Map<String, Map<String, String>> INJURY_TYPE_TRANSLATIONS = new HashMap<>();
  
  static {
    // Hazard Source translations
    Map<String, String> trHazard = new HashMap<>();
    trHazard.put("SMOKE_GAS", "Duman/Gaz");
    trHazard.put("VEHICLE_USE", "Araç Kullanımı");
    trHazard.put("ELECTRICITY", "Elektrik");
    trHazard.put("CHEMICAL", "Kimyasal");
    trHazard.put("MACHINERY", "Makine");
    trHazard.put("TOOL", "Alet");
    trHazard.put("FALLING_OBJECT", "Düşen Nesne");
    trHazard.put("HEAT", "Isı");
    trHazard.put("COLD", "Soğuk");
    trHazard.put("FIRE_EXPLOSION", "Yangın/Patlama");
    trHazard.put("FALL_FROM_HEIGHT", "Yüksekten Düşme");
    HAZARD_SOURCE_TRANSLATIONS.put("tr", trHazard);
    
    Map<String, String> enHazard = new HashMap<>();
    enHazard.put("SMOKE_GAS", "Smoke/Gas");
    enHazard.put("VEHICLE_USE", "Vehicle Use");
    enHazard.put("ELECTRICITY", "Electricity");
    enHazard.put("CHEMICAL", "Chemical");
    enHazard.put("MACHINERY", "Machinery");
    enHazard.put("TOOL", "Tool");
    enHazard.put("FALLING_OBJECT", "Falling Object");
    enHazard.put("HEAT", "Heat");
    enHazard.put("COLD", "Cold");
    enHazard.put("FIRE_EXPLOSION", "Fire/Explosion");
    enHazard.put("FALL_FROM_HEIGHT", "Fall from Height");
    HAZARD_SOURCE_TRANSLATIONS.put("en", enHazard);
    
    Map<String, String> ruHazard = new HashMap<>();
    ruHazard.put("SMOKE_GAS", "Дым/Газ");
    ruHazard.put("VEHICLE_USE", "Использование транспорта");
    ruHazard.put("ELECTRICITY", "Электричество");
    ruHazard.put("CHEMICAL", "Химическое вещество");
    ruHazard.put("MACHINERY", "Машина");
    ruHazard.put("TOOL", "Инструмент");
    ruHazard.put("FALLING_OBJECT", "Падающий предмет");
    ruHazard.put("HEAT", "Тепло");
    ruHazard.put("COLD", "Холод");
    ruHazard.put("FIRE_EXPLOSION", "Пожар/Взрыв");
    ruHazard.put("FALL_FROM_HEIGHT", "Падение с высоты");
    HAZARD_SOURCE_TRANSLATIONS.put("ru", ruHazard);
    
    // Body Part translations
    Map<String, String> trBody = new HashMap<>();
    trBody.put("HAND_FINGER", "El/Parmak");
    trBody.put("WRIST", "Bilek");
    trBody.put("ARM_SHOULDER", "Kol/Omuz");
    trBody.put("HEAD", "Baş");
    trBody.put("EYE", "Göz");
    trBody.put("FACE", "Yüz");
    trBody.put("THROAT", "Boğaz");
    trBody.put("NECK", "Boyun");
    trBody.put("BACK", "Sırt");
    trBody.put("SPINE", "Omurga");
    trBody.put("TOOTH", "Diş");
    trBody.put("CHEST", "Göğüs");
    trBody.put("EAR", "Kulak");
    trBody.put("FOOT_TOE", "Ayak/Ayak Parmağı");
    trBody.put("LEG", "Bacak");
    trBody.put("INTERNAL_ORGANS", "İç Organlar");
    trBody.put("GENITAL", "Cinsel Organ");
    trBody.put("PSYCHOLOGICAL", "Psikolojik");
    trBody.put("OTHER", "Diğer");
    BODY_PART_TRANSLATIONS.put("tr", trBody);
    
    Map<String, String> enBody = new HashMap<>();
    enBody.put("HAND_FINGER", "Hand/Finger");
    enBody.put("WRIST", "Wrist");
    enBody.put("ARM_SHOULDER", "Arm/Shoulder");
    enBody.put("HEAD", "Head");
    enBody.put("EYE", "Eye");
    enBody.put("FACE", "Face");
    enBody.put("THROAT", "Throat");
    enBody.put("NECK", "Neck");
    enBody.put("BACK", "Back");
    enBody.put("SPINE", "Spine");
    enBody.put("TOOTH", "Tooth");
    enBody.put("CHEST", "Chest");
    enBody.put("EAR", "Ear");
    enBody.put("FOOT_TOE", "Foot/Toe");
    enBody.put("LEG", "Leg");
    enBody.put("INTERNAL_ORGANS", "Internal Organs");
    enBody.put("GENITAL", "Genital");
    enBody.put("PSYCHOLOGICAL", "Psychological");
    enBody.put("OTHER", "Other");
    BODY_PART_TRANSLATIONS.put("en", enBody);
    
    Map<String, String> ruBody = new HashMap<>();
    ruBody.put("HAND_FINGER", "Кисть/Палец");
    ruBody.put("WRIST", "Запястье");
    ruBody.put("ARM_SHOULDER", "Рука/Плечо");
    ruBody.put("HEAD", "Голова");
    ruBody.put("EYE", "Глаз");
    ruBody.put("FACE", "Лицо");
    ruBody.put("THROAT", "Горло");
    ruBody.put("NECK", "Шея");
    ruBody.put("BACK", "Спина");
    ruBody.put("SPINE", "Позвоночник");
    ruBody.put("TOOTH", "Зуб");
    ruBody.put("CHEST", "Грудь");
    ruBody.put("EAR", "Ухо");
    ruBody.put("FOOT_TOE", "Стопа/Палец ноги");
    ruBody.put("LEG", "Нога");
    ruBody.put("INTERNAL_ORGANS", "Внутренние органы");
    ruBody.put("GENITAL", "Половые органы");
    ruBody.put("PSYCHOLOGICAL", "Психологический");
    ruBody.put("OTHER", "Другое");
    BODY_PART_TRANSLATIONS.put("ru", ruBody);
    
    // Injury Type translations
    Map<String, String> trInjury = new HashMap<>();
    trInjury.put("CUT", "Kesik");
    trInjury.put("BRUISE", "Çürük");
    trInjury.put("BURN", "Yanık");
    trInjury.put("FRACTURE", "Kırık");
    trInjury.put("SPRAIN", "Burkulma");
    trInjury.put("PUNCTURE_NAIL", "Delinme/Çivi");
    trInjury.put("ELECTRIC_SHOCK", "Elektrik Çarpması");
    trInjury.put("POISONING", "Zehirlenme");
    trInjury.put("ASPIRATION", "Aspirasyon");
    trInjury.put("OTHER", "Diğer");
    INJURY_TYPE_TRANSLATIONS.put("tr", trInjury);
    
    Map<String, String> enInjury = new HashMap<>();
    enInjury.put("CUT", "Cut");
    enInjury.put("BRUISE", "Bruise");
    enInjury.put("BURN", "Burn");
    enInjury.put("FRACTURE", "Fracture");
    enInjury.put("SPRAIN", "Sprain");
    enInjury.put("PUNCTURE_NAIL", "Puncture/Nail");
    enInjury.put("ELECTRIC_SHOCK", "Electric Shock");
    enInjury.put("POISONING", "Poisoning");
    enInjury.put("ASPIRATION", "Aspiration");
    enInjury.put("OTHER", "Other");
    INJURY_TYPE_TRANSLATIONS.put("en", enInjury);
    
    Map<String, String> ruInjury = new HashMap<>();
    ruInjury.put("CUT", "Порез");
    ruInjury.put("BRUISE", "Ушиб");
    ruInjury.put("BURN", "Ожог");
    ruInjury.put("FRACTURE", "Перелом");
    ruInjury.put("SPRAIN", "Растяжение");
    ruInjury.put("PUNCTURE_NAIL", "Прокол/Гвоздь");
    ruInjury.put("ELECTRIC_SHOCK", "Электрошок");
    ruInjury.put("POISONING", "Отравление");
    ruInjury.put("ASPIRATION", "Аспирация");
    ruInjury.put("OTHER", "Другое");
    INJURY_TYPE_TRANSLATIONS.put("ru", ruInjury);
  }
  
  public static String translateHazardSource(String lang, String hazardSource) {
    if (lang == null || lang.isEmpty()) lang = "tr";
    if (hazardSource == null) return "-";
    Map<String, String> langMap = HAZARD_SOURCE_TRANSLATIONS.getOrDefault(lang, HAZARD_SOURCE_TRANSLATIONS.get("tr"));
    return langMap.getOrDefault(hazardSource, hazardSource);
  }
  
  public static String translateBodyPart(String lang, String bodyPart) {
    if (lang == null || lang.isEmpty()) lang = "tr";
    if (bodyPart == null) return "-";
    Map<String, String> langMap = BODY_PART_TRANSLATIONS.getOrDefault(lang, BODY_PART_TRANSLATIONS.get("tr"));
    return langMap.getOrDefault(bodyPart, bodyPart);
  }
  
  public static String translateInjuryType(String lang, String injuryType) {
    if (lang == null || lang.isEmpty()) lang = "tr";
    if (injuryType == null) return "-";
    Map<String, String> langMap = INJURY_TYPE_TRANSLATIONS.getOrDefault(lang, INJURY_TYPE_TRANSLATIONS.get("tr"));
    return langMap.getOrDefault(injuryType, injuryType);
  }
}

