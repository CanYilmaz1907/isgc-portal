package com.isgc.portal.accident.report;

import com.isgc.portal.accident.report.dto.AccidentDistributionResponse;
import com.isgc.portal.accident.report.dto.AccidentRootCausePoint;
import com.isgc.portal.accident.report.dto.AccidentSeriesPoint;
import com.isgc.portal.accident.report.dto.AccidentStatsSummaryResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class AccidentStatsExportService {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
  private final AccidentReportingService reporting;

  public AccidentStatsExportService(AccidentReportingService reporting) {
    this.reporting = reporting;
  }

  public byte[] exportExcel(
      Instant from,
      Instant to,
      UUID projectId,
      String area,
      UUID accidentTypeId,
      String bucket
  ) throws IOException {
    AccidentStatsSummaryResponse summary = reporting.summary(from, to, projectId, area, accidentTypeId);
    List<AccidentSeriesPoint> series = reporting.series(from, to, projectId, area, accidentTypeId, bucket);
    AccidentDistributionResponse dist = reporting.distribution(from, to, projectId, area, accidentTypeId);
    List<AccidentRootCausePoint> rootCauses = reporting.rootCauses(from, to, projectId, area, accidentTypeId, 20);

    try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet summarySheet = wb.createSheet("Ozet");
      int r = 0;
      Row h = summarySheet.createRow(r++);
      h.createCell(0).setCellValue("Metrik");
      h.createCell(1).setCellValue("Deger");
      addRow(summarySheet, r++, "Toplam Olay", summary.total());
      addRow(summarySheet, r++, "LTI (MAJOR)", summary.lti());
      addRow(summarySheet, r++, "FAT (FATAL)", summary.fat());
      addRow(summarySheet, r++, "Near Miss", summary.nearMiss());
      summarySheet.autoSizeColumn(0);
      summarySheet.autoSizeColumn(1);

      Sheet trendSheet = wb.createSheet("Trend");
      Row th = trendSheet.createRow(0);
      th.createCell(0).setCellValue("Donem");
      th.createCell(1).setCellValue("Adet");
      int tr = 1;
      for (AccidentSeriesPoint p : series) {
        Row row = trendSheet.createRow(tr++);
        row.createCell(0).setCellValue(formatInstant(p.bucketStart()));
        row.createCell(1).setCellValue(p.count());
      }
      trendSheet.autoSizeColumn(0);
      trendSheet.autoSizeColumn(1);

      Sheet classSheet = wb.createSheet("Sinif Dagilimi");
      Row ch = classSheet.createRow(0);
      ch.createCell(0).setCellValue("Sinif");
      ch.createCell(1).setCellValue("Adet");
      int cr = 1;
      for (Map.Entry<String, Long> e : dist.byAccidentClass().entrySet()) {
        Row row = classSheet.createRow(cr++);
        row.createCell(0).setCellValue(e.getKey());
        row.createCell(1).setCellValue(e.getValue());
      }
      classSheet.autoSizeColumn(0);
      classSheet.autoSizeColumn(1);

      Sheet potSheet = wb.createSheet("Potansiyel Dagilimi");
      Row ph = potSheet.createRow(0);
      ph.createCell(0).setCellValue("Seviye");
      ph.createCell(1).setCellValue("Adet");
      int pr = 1;
      for (Map.Entry<String, Long> e : dist.byPotentialLevel().entrySet()) {
        Row row = potSheet.createRow(pr++);
        row.createCell(0).setCellValue(e.getKey());
        row.createCell(1).setCellValue(e.getValue());
      }
      potSheet.autoSizeColumn(0);
      potSheet.autoSizeColumn(1);

      Sheet rcSheet = wb.createSheet("Kok Nedenler");
      Row rh = rcSheet.createRow(0);
      rh.createCell(0).setCellValue("Neden");
      rh.createCell(1).setCellValue("Adet");
      int rr = 1;
      for (AccidentRootCausePoint p : rootCauses) {
        Row row = rcSheet.createRow(rr++);
        row.createCell(0).setCellValue(p.label());
        row.createCell(1).setCellValue(p.count());
      }
      rcSheet.autoSizeColumn(0);
      rcSheet.autoSizeColumn(1);

      wb.write(out);
      return out.toByteArray();
    }
  }

  private static void addRow(Sheet sheet, int rowIdx, String label, long value) {
    Row row = sheet.createRow(rowIdx);
    row.createCell(0).setCellValue(label);
    row.createCell(1).setCellValue(value);
  }

  private static String formatInstant(Instant instant) {
    if (instant == null) return "";
    return instant.atZone(ZoneId.systemDefault()).format(DATE_FMT);
  }
}
