package com.isgc.portal.accident;

import com.isgc.portal.accident.dto.AccidentResponse;
import com.isgc.portal.accident.dto.CauseSelectionDto;
import com.isgc.portal.security.CurrentUser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class AccidentListExportService {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private final AccidentService accidentService;

  public AccidentListExportService(AccidentService accidentService) {
    this.accidentService = accidentService;
  }

  public byte[] exportExcel(CurrentUser user) throws IOException {
    return buildWorkbook(accidentService.list(user));
  }

  private static byte[] buildWorkbook(List<AccidentResponse> rows) throws IOException {
    try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = wb.createSheet("Kaza Listesi");
      String[] headers = {
          "No", "Tarih", "Proje", "Sınıflandırma", "Alan", "Tehlike Kaynağı",
          "Yaralanma Türü", "Açıklama", "Süpervizör", "Doğrudan Sebepler", "Kök Sebepler"
      };
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < headers.length; i++) {
        headerRow.createCell(i).setCellValue(headers[i]);
      }

      int rowIndex = 1;
      for (AccidentResponse a : rows) {
        Row row = sheet.createRow(rowIndex++);
        int col = 0;
        row.createCell(col++).setCellValue(a.incidentNo() != null ? a.incidentNo() : 0);
        row.createCell(col++).setCellValue(
            a.occurredAt() != null ? a.occurredAt().atZone(ZoneId.systemDefault()).format(DATE_FMT) : "");
        row.createCell(col++).setCellValue(a.projectName() != null ? a.projectName() : "");
        row.createCell(col++).setCellValue(a.classification() != null ? a.classification() : "");
        row.createCell(col++).setCellValue(a.area() != null ? a.area() : "");
        row.createCell(col++).setCellValue(a.hazardSource() != null ? a.hazardSource() : "");
        row.createCell(col++).setCellValue(a.injuryType() != null ? a.injuryType() : "");
        row.createCell(col++).setCellValue(a.description() != null ? a.description() : "");
        row.createCell(col++).setCellValue(
            a.workSupervisor() != null ? a.workSupervisor()
                : (a.supervisorEmployee() != null
                    ? a.supervisorEmployee().firstName() + " " + a.supervisorEmployee().lastName()
                    : ""));
        row.createCell(col++).setCellValue(formatCauses(a.directCauses()));
        row.createCell(col).setCellValue(formatCauses(a.rootCauses()));
      }

      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }
      wb.write(out);
      return out.toByteArray();
    }
  }

  private static String formatCauses(List<CauseSelectionDto> causes) {
    if (causes == null || causes.isEmpty()) return "";
    return causes.stream()
        .map(c -> c.code() + ": " + c.label())
        .collect(Collectors.joining(", "));
  }
}
