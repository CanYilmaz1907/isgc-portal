package com.isgc.portal.discipline;

import com.isgc.portal.files.FileObject;
import com.isgc.portal.files.FileObjectRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisciplinePdfService {
  private final FileObjectRepository fileRepo;

  public DisciplinePdfService(FileObjectRepository fileRepo) {
    this.fileRepo = fileRepo;
  }

  @Transactional(readOnly = true)
  public byte[] generatePdf(DisciplineLog log) throws IOException {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      PDPageContentStream contentStream = new PDPageContentStream(document, page);
      try {
        float margin = 50;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float lineHeight = 20;
        float currentY = yPosition;

        PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        // Title
        contentStream.beginText();
        contentStream.setFont(titleFont, 16);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText(asciiSafe("DİSİPLİN LOGU RAPORU"));
        contentStream.endText();
        currentY -= lineHeight * 2;

        // Basic Information
        String occurredAtStr = "-";
        if (log.getOccurredAt() != null) {
          occurredAtStr = log.getOccurredAt().atZone(ZoneId.systemDefault())
              .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, "Tarih:", occurredAtStr);
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, "Kategori:", 
            asciiSafe(log.getCategory() != null ? log.getCategory() : "-"));
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, "Seviye:", 
            String.valueOf(log.getSeverity()));
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, "Meslek:", 
            asciiSafe(log.getProfession() != null ? log.getProfession() : "-"));
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, "Durum:", 
            log.getStatus().name());
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, "Calisan:", 
            asciiSafe(log.getViolatingEmployee() != null 
                ? log.getViolatingEmployee().getFirstName() + " " + log.getViolatingEmployee().getLastName() 
                : "-"));
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, "Yonetici:", 
            asciiSafe(log.getViolatingManagerEmployee() != null 
                ? log.getViolatingManagerEmployee().getFirstName() + " " + log.getViolatingManagerEmployee().getLastName() 
                : "-"));
        currentY -= lineHeight;

        // Description
        currentY = addField(contentStream, margin, currentY, lineHeight, headerFont, normalFont, "Aciklama:", "");
        currentY -= lineHeight / 2;
        String description = asciiSafe(log.getDescription() != null ? log.getDescription() : "-");
        String[] descLines = wrapText(description, 80);
        contentStream.beginText();
        contentStream.setFont(normalFont, 10);
        for (String line : descLines) {
          if (currentY < margin + 100) {
            contentStream.endText();
            contentStream.close();
            PDPage newPage = new PDPage(PDRectangle.A4);
            document.addPage(newPage);
            page = newPage;
            contentStream = new PDPageContentStream(document, newPage);
            currentY = page.getMediaBox().getHeight() - margin;
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
          }
          contentStream.newLineAtOffset(margin, currentY);
          contentStream.showText(line);
          contentStream.endText();
          currentY -= lineHeight;
          contentStream.beginText();
          contentStream.setFont(normalFont, 10);
        }
        contentStream.endText();
        currentY -= lineHeight;

        // Photos/Attachments
        List<FileObject> files = fileRepo.findByModuleAndEntityId(DisciplineService.FILE_MODULE, log.getId());
        if (!files.isEmpty()) {
          currentY -= lineHeight;
          contentStream.beginText();
          contentStream.setFont(headerFont, 12);
          contentStream.newLineAtOffset(margin, currentY);
          contentStream.showText(asciiSafe("EK FOTOGRAFLAR/DOSYALAR"));
          contentStream.endText();
          currentY -= lineHeight * 2;

          for (FileObject file : files) {
            if (currentY < margin + 200) {
              contentStream.close();
              PDPage newPage = new PDPage(PDRectangle.A4);
              document.addPage(newPage);
              page = newPage;
              contentStream = new PDPageContentStream(document, newPage);
              currentY = page.getMediaBox().getHeight() - margin;
            }

            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
              try {
                Path filePath = Path.of(file.getStoragePath());
                if (Files.exists(filePath)) {
                  PDImageXObject image = PDImageXObject.createFromFile(filePath.toString(), document);
                  float imageWidth = image.getWidth();
                  float imageHeight = image.getHeight();
                  float maxWidth = page.getMediaBox().getWidth() - 2 * margin;
                  float maxHeight = 200;
                  
                  float scale = Math.min(maxWidth / imageWidth, maxHeight / imageHeight);
                  float scaledWidth = imageWidth * scale;
                  float scaledHeight = imageHeight * scale;

                  contentStream.drawImage(image, margin, currentY - scaledHeight, scaledWidth, scaledHeight);
                  currentY -= scaledHeight + lineHeight;
                }
              } catch (Exception e) {
                // Skip image if can't load
                currentY = addField(contentStream, margin, currentY, lineHeight, normalFont, normalFont, 
                    "Dosya:", asciiSafe(file.getOriginalFilename() + " (yuklenemedi)"));
              }
            } else {
              currentY = addField(contentStream, margin, currentY, lineHeight, normalFont, normalFont, 
                  "Dosya:", asciiSafe(file.getOriginalFilename()));
            }
          }
        }
      } finally {
        if (contentStream != null) {
          try {
            contentStream.endText();
          } catch (Exception e) {
            // Ignore
          }
          contentStream.close();
        }
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      document.save(baos);
      return baos.toByteArray();
    }
  }

  private float addField(PDPageContentStream contentStream, float margin, float y, float lineHeight, 
      PDType1Font headerFont, PDType1Font normalFont, String label, String value) throws IOException {
    contentStream.beginText();
    contentStream.setFont(headerFont, 10);
    contentStream.newLineAtOffset(margin, y);
    contentStream.showText(label);
    contentStream.endText();

    contentStream.beginText();
    contentStream.setFont(normalFont, 10);
    contentStream.newLineAtOffset(margin + 120, y);
    contentStream.showText(value);
    contentStream.endText();

    return y - lineHeight;
  }

  private String[] wrapText(String text, int maxLength) {
    if (text == null || text.isEmpty()) return new String[] { "-" };
    if (text.length() <= maxLength) return new String[] { text };
    
    java.util.List<String> lines = new java.util.ArrayList<>();
    int start = 0;
    while (start < text.length()) {
      int end = Math.min(start + maxLength, text.length());
      if (end < text.length()) {
        int lastSpace = text.lastIndexOf(' ', end);
        if (lastSpace > start) {
          end = lastSpace;
        }
      }
      lines.add(text.substring(start, end).trim());
      start = end;
      while (start < text.length() && text.charAt(start) == ' ') {
        start++;
      }
    }
    return lines.toArray(new String[0]);
  }

  private String asciiSafe(String text) {
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

