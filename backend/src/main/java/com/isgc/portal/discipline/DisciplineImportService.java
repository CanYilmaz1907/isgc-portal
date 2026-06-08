package com.isgc.portal.discipline;

import com.isgc.portal.discipline.dto.DisciplineImportResult;
import com.isgc.portal.discipline.dto.DisciplineUpsertRequest;
import com.isgc.portal.security.CurrentUser;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DisciplineImportService {
  private final DisciplineService disciplineService;
  private final DataFormatter formatter = new DataFormatter();

  public DisciplineImportService(DisciplineService disciplineService) {
    this.disciplineService = disciplineService;
  }

  public DisciplineImportResult importExcel(CurrentUser user, MultipartFile file) throws IOException {
    List<String> errors = new ArrayList<>();
    int imported = 0;

    try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
      Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
      if (sheet == null) {
        return new DisciplineImportResult(0, List.of("Excel dosyasında sayfa bulunamadı"));
      }

      int headerRow = sheet.getFirstRowNum();
      for (int i = headerRow + 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null || isEmptyRow(row)) continue;

        try {
          DisciplineUpsertRequest req = parseRow(row);
          disciplineService.create(user, req);
          imported++;
        } catch (Exception e) {
          errors.add("Satır " + (i + 1) + ": " + e.getMessage());
        }
      }
    }

    return new DisciplineImportResult(imported, errors);
  }

  private DisciplineUpsertRequest parseRow(Row row) {
    String fullName = cell(row, 2);
    if (fullName == null || fullName.isBlank()) {
      throw new IllegalArgumentException("Adı Soyadı zorunludur");
    }

    LocalDate date = parseDate(cell(row, 1));
    DisciplineCategory category = parseCategory(cell(row, 7));
    String violationTypeLabel = cell(row, 8);
    String violationType = resolveViolationType(category, violationTypeLabel);

    return new DisciplineUpsertRequest(
        null,
        date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        fullName,
        blankToNull(cell(row, 3)),
        require(cell(row, 4), "Firma"),
        require(cell(row, 5), "Görevi"),
        require(cell(row, 6), "Çalıştığı Bölge"),
        category,
        violationType,
        require(cell(row, 9), "Uygunsuzluk Açıklaması"),
        require(cell(row, 10), "Sorumlu Kişi"),
        parseStatus(cell(row, 11)),
        null,
        null,
        null,
        null,
        null
    );
  }

  private String cell(Row row, int idx) {
    Cell c = row.getCell(idx);
    if (c == null) return null;
    String v = formatter.formatCellValue(c);
    return v != null ? v.trim() : null;
  }

  private boolean isEmptyRow(Row row) {
    for (int i = 0; i <= 11; i++) {
      String v = cell(row, i);
      if (v != null && !v.isBlank()) return false;
    }
    return true;
  }

  private String require(String value, String field) {
    if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " zorunludur");
    return value.trim();
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private LocalDate parseDate(String raw) {
    if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Tarih zorunludur");
    String v = raw.trim();
    if (v.contains(".")) {
      String[] p = v.split("\\.");
      if (p.length == 3) {
        return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
      }
    }
    if (v.contains("-")) {
      return LocalDate.parse(v.substring(0, Math.min(10, v.length())));
    }
    throw new IllegalArgumentException("Geçersiz tarih: " + raw);
  }

  private DisciplineCategory parseCategory(String raw) {
    if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Kategori zorunludur");
    String v = raw.trim().toLowerCase();
    if (v.startsWith("0") || v.contains("direkt")) return DisciplineCategory.CAT_0;
    if (v.startsWith("1")) return DisciplineCategory.CAT_1;
    if (v.startsWith("2")) return DisciplineCategory.CAT_2;
    if (v.startsWith("3")) return DisciplineCategory.CAT_3;
    if (v.equals("cat_0")) return DisciplineCategory.CAT_0;
    if (v.equals("cat_1")) return DisciplineCategory.CAT_1;
    if (v.equals("cat_2")) return DisciplineCategory.CAT_2;
    if (v.equals("cat_3")) return DisciplineCategory.CAT_3;
    throw new IllegalArgumentException("Geçersiz kategori: " + raw);
  }

  private String resolveViolationType(DisciplineCategory category, String label) {
    if (label == null || label.isBlank()) {
      throw new IllegalArgumentException("Uygunsuzluk Tipi zorunludur");
    }
    String trimmed = label.trim();
    for (var opt : DisciplineViolationTypes.forCategory(category)) {
      if (opt.labelTr().equalsIgnoreCase(trimmed) || opt.code().equalsIgnoreCase(trimmed)) {
        return opt.code();
      }
    }
    throw new IllegalArgumentException("Kategori için geçersiz uygunsuzluk tipi: " + label);
  }

  private DisciplineStatus parseStatus(String raw) {
    if (raw == null || raw.isBlank()) return DisciplineStatus.UYARI;
    String v = raw.trim().toUpperCase();
    if (v.contains("SÖZLÜ") || v.contains("SOZLU")) return DisciplineStatus.SOZLU_UYARI;
    if (v.contains("İDARİ") || v.contains("IDARI") || v.contains("CEZA")) return DisciplineStatus.IDARI_CEZA;
    if (v.contains("FESH") || v.contains("ÇIKIŞ") || v.contains("CIKIS")) return DisciplineStatus.SOZLESME_FESHI;
    if (v.contains("UYARI")) return DisciplineStatus.UYARI;
    return DisciplineStatus.UYARI;
  }
}
