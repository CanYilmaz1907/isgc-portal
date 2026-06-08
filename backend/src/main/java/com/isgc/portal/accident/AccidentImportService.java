package com.isgc.portal.accident;

import com.isgc.portal.accident.dto.AccidentImportResult;
import com.isgc.portal.accident.dto.CauseSelectionDto;
import com.isgc.portal.files.FileObjectRepository;
import com.isgc.portal.project.Project;
import com.isgc.portal.project.ProjectRepository;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AccidentImportService {
  private static final Logger log = LoggerFactory.getLogger(AccidentImportService.class);
  private static final Pattern CAUSE_CODE = Pattern.compile("^(\\d+-\\d+)\\s*(.*)$");
  private static final int DATA_START_ROW = 3;
  private static final String GROUP_COMPANY_NAME = "EASTCON TECH";

  private final ProjectRepository projectRepository;
  private final AccidentRepository accidentRepository;
  private final AccidentTypeRepository accidentTypeRepository;
  private final AccidentDirectCauseRepository directCauseRepository;
  private final AccidentRootCauseRepository rootCauseRepository;
  private final AccidentPersonRepository accidentPersonRepository;
  private final FileObjectRepository fileObjectRepository;
  private final CauseCategoryRepository causeCategoryRepository;
  private final EntityManager entityManager;
  private final DataFormatter formatter = new DataFormatter();

  public AccidentImportService(
      ProjectRepository projectRepository,
      AccidentRepository accidentRepository,
      AccidentTypeRepository accidentTypeRepository,
      AccidentDirectCauseRepository directCauseRepository,
      AccidentRootCauseRepository rootCauseRepository,
      AccidentPersonRepository accidentPersonRepository,
      FileObjectRepository fileObjectRepository,
      CauseCategoryRepository causeCategoryRepository,
      EntityManager entityManager
  ) {
    this.projectRepository = projectRepository;
    this.accidentRepository = accidentRepository;
    this.accidentTypeRepository = accidentTypeRepository;
    this.directCauseRepository = directCauseRepository;
    this.rootCauseRepository = rootCauseRepository;
    this.accidentPersonRepository = accidentPersonRepository;
    this.fileObjectRepository = fileObjectRepository;
    this.causeCategoryRepository = causeCategoryRepository;
    this.entityManager = entityManager;
  }

  @Transactional
  public AccidentImportResult importExcel(String projectCode, boolean replaceExisting, MultipartFile file)
      throws IOException {
    Project project = projectRepository.findByCode(projectCode)
        .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectCode));

    int deleted = 0;
    if (replaceExisting) {
      deleted = deleteAllAccidents();
    }

    Map<String, String> directLabels = loadCauseLabels("DIRECT");
    Map<String, String> rootLabels = loadCauseLabels("ROOT");
    Map<String, AccidentType> typeByCode = indexAccidentTypes();

    try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
      Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
      if (sheet == null) {
        return new AccidentImportResult(deleted, 0, List.of("Excel dosyasında sayfa bulunamadı"));
      }

      Map<Long, String> mergedValues = buildMergedValueCache(sheet);

      List<String> errors = new ArrayList<>();
      int imported = 0;

      ParsedIncident current = null;
      int autoIncidentNo = 1;
      for (int i = DATA_START_ROW; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;
        if (isBlankRow(sheet, row, mergedValues)) continue;

        try {
          if (isMainRow(sheet, row, mergedValues)) {
            if (current != null) {
              persistIncident(current, project, typeByCode, directLabels, rootLabels);
              imported++;
            }
            current = parseMainRow(sheet, row, mergedValues, directLabels, rootLabels);
            if (current.incidentNo == null || current.incidentNo == 0) {
              current.incidentNo = autoIncidentNo;
            }
            autoIncidentNo = Math.max(autoIncidentNo, current.incidentNo + 1);
          } else if (current != null) {
            appendCauseRow(current, sheet, row, mergedValues, directLabels, rootLabels);
          }
        } catch (Exception e) {
          errors.add("Satır " + (i + 1) + ": " + e.getMessage());
          log.warn("Import error row {}: {}", i + 1, e.getMessage());
        }
      }
      if (current != null) {
        try {
          persistIncident(current, project, typeByCode, directLabels, rootLabels);
          imported++;
        } catch (Exception e) {
          errors.add("Son kayıt: " + e.getMessage());
        }
      }

      syncIncidentSequence();
      log.info("Accident import for {}: deleted={}, imported={}, errors={}", projectCode, deleted, imported, errors.size());
      return new AccidentImportResult(deleted, imported, errors);
    }
  }

  private Map<Long, String> buildMergedValueCache(Sheet sheet) {
    Map<Long, String> cache = new HashMap<>();
    for (CellRangeAddress region : sheet.getMergedRegions()) {
      Row firstRow = sheet.getRow(region.getFirstRow());
      if (firstRow == null) continue;
      Cell firstCell = firstRow.getCell(region.getFirstColumn());
      if (firstCell == null) continue;
      String value = formatter.formatCellValue(firstCell);
      if (value == null || value.isBlank()) continue;
      value = value.trim();
      for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
        for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
          cache.put(pack(r, c), value);
        }
      }
    }
    return cache;
  }

  private static long pack(int row, int col) {
    return ((long) row << 32) | (col & 0xffffffffL);
  }

  private int deleteAllAccidents() {
    List<Accident> accidents = accidentRepository.findAll();
    for (Accident a : accidents) {
      deleteAccident(a);
    }
    return accidents.size();
  }

  private void deleteAccident(Accident a) {
    UUID id = a.getId();
    fileObjectRepository.findByModuleAndEntityId(AccidentService.FILE_MODULE, id)
        .forEach(fileObjectRepository::delete);
    directCauseRepository.deleteByAccidentId(id);
    rootCauseRepository.deleteByAccidentId(id);
    accidentPersonRepository.deleteByAccidentId(id);
    accidentRepository.deleteById(id);
  }

  private Map<String, String> loadCauseLabels(String causeType) {
    Map<String, String> map = new HashMap<>();
    for (CauseCategory c : causeCategoryRepository.findByCauseTypeAndEnabledTrueOrderBySortOrderAsc(causeType)) {
      map.put(c.getItemCode(), c.getItemLabel());
    }
    return map;
  }

  private Map<String, AccidentType> indexAccidentTypes() {
    Map<String, AccidentType> map = new LinkedHashMap<>();
    for (AccidentType t : accidentTypeRepository.findByEnabledTrue()) {
      map.put(t.getCode(), t);
    }
    return map;
  }

  private boolean isMainRow(Sheet sheet, Row row, Map<Long, String> mergedValues) {
    return cellDirect(row, 3) != null;
  }

  private boolean isBlankRow(Sheet sheet, Row row, Map<Long, String> mergedValues) {
    for (int c = 0; c <= 22; c++) {
      String v = cell(sheet, row, c, mergedValues);
      if (v != null && !v.isBlank()) return false;
    }
    return true;
  }

  private ParsedIncident parseMainRow(
      Sheet sheet,
      Row row,
      Map<Long, String> mergedValues,
      Map<String, String> directLabels,
      Map<String, String> rootLabels
  ) {
    ParsedIncident p = new ParsedIncident();
    p.incidentNo = parseInteger(cell(sheet, row, 0, mergedValues));
    p.employeeRegistrationNo = blankToNull(cell(sheet, row, 1, mergedValues));
    p.classificationLabel = blankToNull(cell(sheet, row, 3, mergedValues));
    p.area = blankToNull(cell(sheet, row, 4, mergedValues));
    p.projectLabel = blankToNull(cell(sheet, row, 2, mergedValues));
    p.occurredAt = parseDate(sheet, row, 5, mergedValues);
    p.timePeriod = blankToNull(cell(sheet, row, 6, mergedValues));
    p.hazardSource = blankToNull(cell(sheet, row, 7, mergedValues));
    p.injuredBodyPart = blankToNull(cell(sheet, row, 8, mergedValues));
    p.injuryType = blankToNull(cell(sheet, row, 9, mergedValues));
    p.description = blankToNull(cell(sheet, row, 10, mergedValues));
    p.workSupervisor = blankToNull(cell(sheet, row, 15, mergedValues));
    p.personName = blankToNull(cell(sheet, row, 16, mergedValues));
    p.personAge = parseInteger(cell(sheet, row, 17, mergedValues));
    p.personJobTitle = blankToNull(cell(sheet, row, 18, mergedValues));
    p.personNationality = blankToNull(cell(sheet, row, 19, mergedValues));
    p.durationOnProject = blankToNull(cell(sheet, row, 20, mergedValues));
    p.durationInRole = blankToNull(cell(sheet, row, 21, mergedValues));
    p.personCompany = blankToNull(cell(sheet, row, 22, mergedValues));
    appendCauseRow(p, sheet, row, mergedValues, directLabels, rootLabels);
    return p;
  }

  private void appendCauseRow(
      ParsedIncident p,
      Sheet sheet,
      Row row,
      Map<Long, String> mergedValues,
      Map<String, String> directLabels,
      Map<String, String> rootLabels
  ) {
    addCause(p.directCauseCodes, cell(sheet, row, 12, mergedValues), directLabels);
    addCause(p.rootCauseCodes, cell(sheet, row, 14, mergedValues), rootLabels);
  }

  private void addCause(Set<String> target, String raw, Map<String, String> labelByCode) {
    CauseSelectionDto dto = parseCause(raw, labelByCode);
    if (dto != null) {
      target.add(dto.code() + "\0" + dto.label());
    }
  }

  private CauseSelectionDto parseCause(String raw, Map<String, String> labelByCode) {
    if (raw == null || raw.isBlank()) return null;
    String trimmed = raw.trim();
    Matcher m = CAUSE_CODE.matcher(trimmed);
    if (!m.find()) return null;
    String code = m.group(1);
    String tail = m.group(2) != null ? m.group(2).trim() : "";
    String label = labelByCode.getOrDefault(code, tail);
    if (label == null || label.isBlank()) label = trimmed;
    return new CauseSelectionDto(code, label);
  }

  private void persistIncident(
      ParsedIncident p,
      Project project,
      Map<String, AccidentType> typeByCode,
      Map<String, String> directLabels,
      Map<String, String> rootLabels
  ) {
    if (p.incidentNo == null) {
      p.incidentNo = 0;
    }

    String classification = mapClassification(p.classificationLabel);
    AccidentType type = resolveAccidentType(p.classificationLabel, typeByCode);

    Accident a = new Accident();
    a.setId(UUID.randomUUID());
    a.setProject(project);
    a.setAccidentType(type);
    a.setIncidentNo(p.incidentNo);
    a.setClassification(classification);
    a.setAccidentClass(mapAccidentClass(classification));
    a.setPotentialLevel(PotentialLevel.LOW);
    a.setStatus(AccidentStatus.CLOSED);
    a.setOccurredAt(p.occurredAt);
    a.setArea(p.area);
    a.setLocation(p.area);
    a.setTimePeriod(p.timePeriod);
    a.setHazardSource(normalizeHazardSource(p.hazardSource));
    a.setGroupCompanyName(GROUP_COMPANY_NAME);
    a.setInjuredBodyPart(p.injuredBodyPart);
    a.setInjuryType(p.injuryType);
    a.setDescription(p.description);
    a.setEmployeeRegistrationNo(p.employeeRegistrationNo);
    a.setWorkSupervisor(p.workSupervisor);
    a.setPersonName(p.personName);
    a.setInjuredPersonAge(p.personAge);
    a.setInjuredPersonProfession(p.personJobTitle);
    a.setInjuredPersonNationality(p.personNationality);
    a.setDurationOnProject(p.durationOnProject);
    a.setDurationInRole(p.durationInRole);
    a.setInjuredPersonCompany(p.personCompany);
    String projectLabel = p.projectLabel != null ? p.projectLabel : project.getName();
    a.setFormData("{\"projectName\":\"" + jsonEscape(projectLabel) + "\"}");
    a.setRootCauseData("{}");
    a.setActionsTaken("[]");
    a.setWorkRelated(true);
    accidentRepository.save(a);

    for (String key : p.directCauseCodes) {
      saveCause(a, key, true, directLabels);
    }
    for (String key : p.rootCauseCodes) {
      saveCause(a, key, false, rootLabels);
    }
  }

  private void saveCause(Accident a, String key, boolean direct, Map<String, String> labelByCode) {
    int sep = key.indexOf('\0');
    String code = sep >= 0 ? key.substring(0, sep) : key;
    String label = sep >= 0 ? key.substring(sep + 1) : labelByCode.getOrDefault(code, code);
    if (direct) {
      AccidentDirectCause dc = new AccidentDirectCause();
      dc.setId(UUID.randomUUID());
      dc.setAccident(a);
      dc.setCauseCode(code);
      dc.setCauseLabel(label);
      directCauseRepository.save(dc);
    } else {
      AccidentRootCause rc = new AccidentRootCause();
      rc.setId(UUID.randomUUID());
      rc.setAccident(a);
      rc.setCauseCode(code);
      rc.setCauseLabel(label);
      rootCauseRepository.save(rc);
    }
  }

  private String mapClassification(String label) {
    if (label == null || label.isBlank()) return "NEAR_MISS";
    String n = label.trim().toLowerCase();
    if (n.contains("ölüm") || n.contains("fat")) return "FAT";
    if (n.contains("kayıp günlü") || n.equals("lti")) return "LTI";
    if (n.contains("kalıcı sakat")) return "PERMANENT_DISABILITY";
    if (n.contains("kısıtlı iş")) return "RWC";
    if (n.contains("tıbbi müdahale")) return "MTC";
    if (n.contains("ilk yardım")) return "FAC";
    if (n.contains("ucuz atlatma") || n.contains("near miss")) return "NEAR_MISS";
    if (n.contains("mal ekipman")) return "EQUIPMENT";
    if (n.contains("araç") || n.contains("trafik")) return "TRAFFIC";
    if (n.contains("çevre")) return "ENVIRONMENT";
    if (n.contains("yangın")) return "FIRE";
    return "NEAR_MISS";
  }

  private AccidentClass mapAccidentClass(String classification) {
    return switch (classification) {
      case "FAT" -> AccidentClass.FATAL;
      case "LTI", "PERMANENT_DISABILITY", "RWC" -> AccidentClass.MAJOR;
      case "NEAR_MISS" -> AccidentClass.NEAR_MISS;
      default -> AccidentClass.MINOR;
    };
  }

  private AccidentType resolveAccidentType(String label, Map<String, AccidentType> typeByCode) {
    if (label != null) {
      String n = label.trim().toLowerCase();
      if (n.contains("mal ekipman") && typeByCode.containsKey("MAL_EKIPMAN_KAZASI")) {
        return typeByCode.get("MAL_EKIPMAN_KAZASI");
      }
      if (n.contains("tıbbi müdahale") && typeByCode.containsKey("TIBBI_MUDAHALE")) {
        return typeByCode.get("TIBBI_MUDAHALE");
      }
      if (n.contains("ucuz atlatma") && typeByCode.containsKey("UCUZ_ATLATMA")) {
        return typeByCode.get("UCUZ_ATLATMA");
      }
      if (n.contains("ilk yardım") && typeByCode.containsKey("ILK_YARDIM")) {
        return typeByCode.get("ILK_YARDIM");
      }
      if (n.contains("yangın") && typeByCode.containsKey("YANGIN")) {
        return typeByCode.get("YANGIN");
      }
      if ((n.contains("araç") || n.contains("trafik")) && typeByCode.containsKey("MOTORLU_ARAC_KAZASI")) {
        return typeByCode.get("MOTORLU_ARAC_KAZASI");
      }
    }
    return typeByCode.values().stream().findFirst()
        .orElseThrow(() -> new IllegalStateException("No accident type configured"));
  }

  private String normalizeHazardSource(String raw) {
    if (raw == null) return null;
    String v = raw.trim();
    if (v.equalsIgnoreCase("El aleti kullanımı")) return "El Aleti Kullanımı";
    if (v.equalsIgnoreCase("Malzeme transportu")) return "Malzeme Transportu";
    return v;
  }

  private String jsonEscape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private Instant parseDate(Sheet sheet, Row row, int col, Map<Long, String> mergedValues) {
    Cell c = row.getCell(col);
    if (c != null) {
      try {
        if (DateUtil.isCellDateFormatted(c)) {
          return c.getDateCellValue().toInstant();
        }
        if (c.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
          return DateUtil.getJavaDate(c.getNumericCellValue()).toInstant();
        }
      } catch (Exception ignored) {
        // fall through to string parse
      }
    }
    String raw = cell(sheet, row, col, mergedValues);
    if (raw == null) return null;
    raw = raw.trim();
    if (raw.isBlank()) return null;
    for (DateTimeFormatter fmt : List.of(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("M/d/yyyy")
    )) {
      try {
        return LocalDate.parse(raw, fmt).atStartOfDay(ZoneId.systemDefault()).toInstant();
      } catch (Exception ignored) {
        // try next
      }
    }
    return null;
  }

  private Integer parseInteger(String raw) {
    if (raw == null || raw.isBlank()) return null;
    try {
      String v = raw.trim().replace(",", ".");
      if (v.contains(".")) {
        return (int) Double.parseDouble(v);
      }
      return Integer.parseInt(v);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private String cellDirect(Row row, int idx) {
    if (row == null) return null;
    Cell c = row.getCell(idx);
    if (c == null) return null;
    String v = formatter.formatCellValue(c);
    return v != null && !v.isBlank() ? v.trim() : null;
  }

  private String cell(Sheet sheet, Row row, int idx, Map<Long, String> mergedValues) {
    if (row == null) return null;
    Cell c = row.getCell(idx);
    if (c != null) {
      String v = formatter.formatCellValue(c);
      if (v != null && !v.isBlank()) return v.trim();
    }
    String merged = mergedValues.get(pack(row.getRowNum(), idx));
    return merged != null && !merged.isBlank() ? merged.trim() : null;
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private void syncIncidentSequence() {
    entityManager.createNativeQuery(
        "SELECT setval('accidents_incident_no_seq', GREATEST(COALESCE((SELECT MAX(incident_no) FROM accidents), 0), 1))"
    ).getSingleResult();
  }

  private static final class ParsedIncident {
    Integer incidentNo;
    String employeeRegistrationNo;
    String projectLabel;
    String classificationLabel;
    String area;
    Instant occurredAt;
    String timePeriod;
    String hazardSource;
    String injuredBodyPart;
    String injuryType;
    String description;
    String workSupervisor;
    String personName;
    Integer personAge;
    String personJobTitle;
    String personNationality;
    String durationOnProject;
    String durationInRole;
    String personCompany;
    Set<String> directCauseCodes = new HashSet<>();
    Set<String> rootCauseCodes = new HashSet<>();
  }
}
