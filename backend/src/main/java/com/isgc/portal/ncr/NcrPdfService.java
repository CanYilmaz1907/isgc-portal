package com.isgc.portal.ncr;

import com.isgc.portal.ncr.dto.NcrResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
public class NcrPdfService {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  public byte[] generatePdf(Ncr entity, NcrResponse dto, String lang) throws IOException {
    Map<String, String> labels = labelsFor(lang);
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      PDPageContentStream contentStream = new PDPageContentStream(document, page);
      try {
        float margin = 50;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float lineHeight = 18;
        float currentY = yPosition;

        PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        // Title: NCR - Sistemsel Uyumsuzluk Raporu
        contentStream.beginText();
        contentStream.setFont(titleFont, 16);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText(asciiSafe(labels.get("title")));
        contentStream.endText();
        currentY -= lineHeight;

        contentStream.beginText();
        contentStream.setFont(titleFont, 14);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText(asciiSafe(dto.ncrNumber()));
        contentStream.endText();
        currentY -= lineHeight * 2;

        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont,
            labels.get("ncrNumber"), asciiSafe(dto.ncrNumber()));
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont,
            labels.get("ncrDate"), dto.ncrDate() != null ? dto.ncrDate().format(DATE_FMT) : "-");
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont,
            labels.get("project"), asciiSafe(dto.projectName() != null ? dto.projectName() : "-"));
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont,
            labels.get("location"), asciiSafe(dto.location() != null ? dto.location() : "-"));
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont,
            labels.get("titleField"), asciiSafe(dto.title() != null ? dto.title() : "-"));
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont,
            labels.get("classification"), asciiSafe(dto.classification() != null ? dto.classification() : "-"));
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont,
            labels.get("status"), dto.status().name());
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont,
            labels.get("responsible"), asciiSafe(dto.responsibleEmployeeName() != null ? dto.responsibleEmployeeName() : "-"));
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont,
            labels.get("dueDate"), dto.targetCompletionDate() != null ? dto.targetCompletionDate().format(DATE_FMT) : "-");
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont,
            labels.get("closedDate"), dto.completionDate() != null ? dto.completionDate().format(DATE_FMT) : "-");
        currentY -= lineHeight;

        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, labels.get("description"), "");
        currentY -= lineHeight / 2;
        String description = asciiSafe(dto.description() != null ? dto.description() : "-");
        for (String line : wrapText(description, 85)) {
          contentStream.beginText();
          contentStream.setFont(normalFont, 10);
          contentStream.newLineAtOffset(margin, currentY);
          contentStream.showText(line);
          contentStream.endText();
          currentY -= lineHeight;
        }
        currentY -= lineHeight;

        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, labels.get("rootCause"), "");
        currentY -= lineHeight / 2;
        String rootCause = asciiSafe(dto.rootCause() != null ? dto.rootCause() : "-");
        for (String line : wrapText(rootCause, 85)) {
          contentStream.beginText();
          contentStream.setFont(normalFont, 10);
          contentStream.newLineAtOffset(margin, currentY);
          contentStream.showText(line);
          contentStream.endText();
          currentY -= lineHeight;
        }
        currentY -= lineHeight;

        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, labels.get("correctiveAction"), "");
        currentY -= lineHeight / 2;
        String corrective = asciiSafe(dto.proposedCorrectiveAction() != null ? dto.proposedCorrectiveAction() : "-");
        for (String line : wrapText(corrective, 85)) {
          contentStream.beginText();
          contentStream.setFont(normalFont, 10);
          contentStream.newLineAtOffset(margin, currentY);
          contentStream.showText(line);
          contentStream.endText();
          currentY -= lineHeight;
        }
        currentY -= lineHeight;

        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, labels.get("preventiveAction"), "");
        currentY -= lineHeight / 2;
        String preventive = asciiSafe(dto.preventiveAction() != null ? dto.preventiveAction() : "-");
        for (String line : wrapText(preventive, 85)) {
          contentStream.beginText();
          contentStream.setFont(normalFont, 10);
          contentStream.newLineAtOffset(margin, currentY);
          contentStream.showText(line);
          contentStream.endText();
          currentY -= lineHeight;
        }
      } finally {
        try { contentStream.endText(); } catch (Exception ignored) {}
        contentStream.close();
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      document.save(baos);
      return baos.toByteArray();
    }
  }

  private static Map<String, String> labelsFor(String lang) {
    Map<String, String> m = new HashMap<>();
    if ("en".equals(lang)) {
      m.put("title", "NCR - Non-Conformance Report (System Nonconformity)");
      m.put("ncrNumber", "NCR No:");
      m.put("ncrDate", "Date:");
      m.put("project", "Project:");
      m.put("location", "Location:");
      m.put("titleField", "Title:");
      m.put("description", "Description:");
      m.put("classification", "Classification:");
      m.put("rootCause", "Root Cause:");
      m.put("correctiveAction", "Corrective Action:");
      m.put("preventiveAction", "Preventive Action:");
      m.put("responsible", "Responsible:");
      m.put("dueDate", "Due Date:");
      m.put("closedDate", "Closed Date:");
      m.put("status", "Status:");
    } else if ("ru".equals(lang)) {
      m.put("title", "NCR - Otchet o nesootvetstvii (sistemnoye)");
      m.put("ncrNumber", "NCR No:");
      m.put("ncrDate", "Data:");
      m.put("project", "Proyekt:");
      m.put("location", "Mestopolozheniye:");
      m.put("titleField", "Nazvaniye:");
      m.put("description", "Opisaniye:");
      m.put("classification", "Klassifikatsiya:");
      m.put("rootCause", "Kornevaya prichina:");
      m.put("correctiveAction", "Korrekiruyushchiye deystviya:");
      m.put("preventiveAction", "Preduprezhdayushchiye deystviya:");
      m.put("responsible", "Otvetstvennyy:");
      m.put("dueDate", "Srok:");
      m.put("closedDate", "Data zakrytiya:");
      m.put("status", "Status:");
    } else {
      m.put("title", "NCR - Sistemsel Uyumsuzluk Raporu");
      m.put("ncrNumber", "NCR No:");
      m.put("ncrDate", "Tarih:");
      m.put("project", "Proje:");
      m.put("location", "Lokasyon:");
      m.put("titleField", "Baslik:");
      m.put("description", "Aciklama:");
      m.put("classification", "Siniflandirma:");
      m.put("rootCause", "Kok Neden:");
      m.put("correctiveAction", "Duzeltici Aksiyon:");
      m.put("preventiveAction", "Onleyici Aksiyon:");
      m.put("responsible", "Sorumlu:");
      m.put("dueDate", "Son Tarih:");
      m.put("closedDate", "Kapanis Tarihi:");
      m.put("status", "Durum:");
    }
    return m;
  }

  private float addField(PDPageContentStream contentStream, float margin, float y, float lineHeight,
      PDType1Font headerFont, PDType1Font normalFont, String label, String value) throws IOException {
    contentStream.beginText();
    contentStream.setFont(headerFont, 10);
    contentStream.newLineAtOffset(margin, y);
    contentStream.showText(asciiSafe(label));
    contentStream.endText();
    contentStream.beginText();
    contentStream.setFont(normalFont, 10);
    contentStream.newLineAtOffset(margin + 140, y);
    contentStream.showText(asciiSafe(value));
    contentStream.endText();
    return y - lineHeight;
  }

  private static String[] wrapText(String text, int maxLength) {
    if (text == null || text.isEmpty()) return new String[] { "-" };
    if (text.length() <= maxLength) return new String[] { text };
    java.util.List<String> lines = new java.util.ArrayList<>();
    int start = 0;
    while (start < text.length()) {
      int end = Math.min(start + maxLength, text.length());
      if (end < text.length()) {
        int lastSpace = text.lastIndexOf(' ', end);
        if (lastSpace > start) end = lastSpace;
      }
      lines.add(text.substring(start, end).trim());
      start = end;
      while (start < text.length() && text.charAt(start) == ' ') start++;
    }
    return lines.toArray(new String[0]);
  }

  private static String asciiSafe(String text) {
    if (text == null) return "";
    String s = text
        .replace("\u2116", "No.")  // № (NUMERO SIGN) not in Helvetica WinAnsiEncoding
        .replace("İ", "I").replace("ı", "i")
        .replace("Ğ", "G").replace("ğ", "g")
        .replace("Ü", "U").replace("ü", "u")
        .replace("Ş", "S").replace("ş", "s")
        .replace("Ö", "O").replace("ö", "o")
        .replace("Ç", "C").replace("ç", "c");
    // Cyrillic -> Latin (Helvetica WinAnsiEncoding does not support Cyrillic)
    s = s.replace("А", "A").replace("а", "a").replace("Б", "B").replace("б", "b")
        .replace("В", "V").replace("в", "v").replace("Г", "G").replace("г", "g")
        .replace("Д", "D").replace("д", "d").replace("Е", "E").replace("е", "e")
        .replace("Ё", "E").replace("ё", "e").replace("Ж", "Zh").replace("ж", "zh")
        .replace("З", "Z").replace("з", "z").replace("И", "I").replace("и", "i")
        .replace("Й", "Y").replace("й", "y").replace("К", "K").replace("к", "k")
        .replace("Л", "L").replace("л", "l").replace("М", "M").replace("м", "m")
        .replace("Н", "N").replace("н", "n").replace("О", "O").replace("о", "o")
        .replace("П", "P").replace("п", "p").replace("Р", "R").replace("р", "r")
        .replace("С", "S").replace("с", "s").replace("Т", "T").replace("т", "t")
        .replace("У", "U").replace("у", "u").replace("Ф", "F").replace("ф", "f")
        .replace("Х", "Kh").replace("х", "kh").replace("Ц", "Ts").replace("ц", "ts")
        .replace("Ч", "Ch").replace("ч", "ch").replace("Ш", "Sh").replace("ш", "sh")
        .replace("Щ", "Shch").replace("щ", "shch").replace("Ъ", "").replace("ъ", "")
        .replace("Ы", "Y").replace("ы", "y").replace("Ь", "").replace("ь", "")
        .replace("Э", "E").replace("э", "e").replace("Ю", "Yu").replace("ю", "yu")
        .replace("Я", "Ya").replace("я", "ya");
    return s;
  }
}
