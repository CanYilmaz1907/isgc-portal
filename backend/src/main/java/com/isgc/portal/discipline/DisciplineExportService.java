package com.isgc.portal.discipline;

import com.isgc.portal.discipline.dto.DisciplineResponse;
import com.isgc.portal.security.CurrentUser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class DisciplineExportService {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private final DisciplineService disciplineService;

  public DisciplineExportService(DisciplineService disciplineService) {
    this.disciplineService = disciplineService;
  }

  public byte[] exportExcel(CurrentUser user) throws IOException {
    return buildWorkbook(disciplineService.list(user));
  }

  private static byte[] buildWorkbook(List<DisciplineResponse> rows) throws IOException {
    try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = wb.createSheet("Disiplin Logu");
      String[] headers = {
          "S.No", "Tarih", "Adı Soyadı", "Sicil No", "Firma", "Görevi", "Çalıştığı Bölge",
          "Kategori", "Uygunsuzluk Tipi", "Açıklama", "Sorumlu", "Durum", "Tekrar", "Ceza"
      };
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < headers.length; i++) {
        headerRow.createCell(i).setCellValue(headers[i]);
      }

      int rowIndex = 1;
      for (DisciplineResponse d : rows) {
        Row row = sheet.createRow(rowIndex++);
        int col = 0;
        row.createCell(col++).setCellValue(d.sequenceNo() != null ? d.sequenceNo() : 0);
        row.createCell(col++).setCellValue(
            d.occurredAt() != null ? d.occurredAt().atZone(ZoneId.systemDefault()).format(DATE_FMT) : "");
        row.createCell(col++).setCellValue(d.fullName() != null ? d.fullName() : "");
        row.createCell(col++).setCellValue(d.employeeRegistrationNo() != null ? d.employeeRegistrationNo() : "");
        row.createCell(col++).setCellValue(d.company() != null ? d.company() : "");
        row.createCell(col++).setCellValue(d.jobTitle() != null ? d.jobTitle() : "");
        row.createCell(col++).setCellValue(d.workArea() != null ? d.workArea() : "");
        row.createCell(col++).setCellValue(d.categoryLevel() != null ? d.categoryLevel().name() : "");
        row.createCell(col++).setCellValue(d.violationTypeLabel() != null ? d.violationTypeLabel() : "");
        row.createCell(col++).setCellValue(d.violationDescription() != null ? d.violationDescription() : "");
        row.createCell(col++).setCellValue(d.responsiblePerson() != null ? d.responsiblePerson() : "");
        row.createCell(col++).setCellValue(d.status() != null ? d.status().name() : "");
        row.createCell(col++).setCellValue(d.repeatCount());
        row.createCell(col).setCellValue(d.penaltyAmount() != null ? d.penaltyAmount().doubleValue() : 0);
      }

      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }
      wb.write(out);
      return out.toByteArray();
    }
  }
}
