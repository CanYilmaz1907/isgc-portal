package com.isgc.portal.accident;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isgc.portal.accident.dto.AccidentResponse;
import com.isgc.portal.files.FileObject;
import com.isgc.portal.files.FileObjectRepository;
import com.isgc.portal.files.FileStorageProperties;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccidentPdfService {
  private final FileObjectRepository fileRepo;
  private final ObjectMapper objectMapper;
  private final FileStorageProperties storageProperties;

  public AccidentPdfService(FileObjectRepository fileRepo, FileStorageProperties storageProperties) {
    this.fileRepo = fileRepo;
    this.objectMapper = new ObjectMapper();
    this.storageProperties = storageProperties;
  }

  @Transactional(readOnly = true)
  public byte[] generatePdf(AccidentResponse accident, List<AccidentPerson> injured, List<AccidentPerson> keyPeople, String lang) throws IOException {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      PDPageContentStream contentStream = new PDPageContentStream(document, page);
      PDPage[] currentPageRef = new PDPage[] { page };
      PDPageContentStream[] contentStreamRef = new PDPageContentStream[] { contentStream };
      try {
        float margin = 30;
        float topMargin = 50;
        float bottomMargin = 50;
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        float contentWidth = pageWidth - 2 * margin;
        float yPosition = pageHeight - topMargin;
        float lineHeight = 14;
        float currentY = yPosition;

        // Load fonts - use TrueType for Cyrillic support (Russian)
        org.apache.pdfbox.pdmodel.font.PDFont titleFont;
        org.apache.pdfbox.pdmodel.font.PDFont headerFont;
        org.apache.pdfbox.pdmodel.font.PDFont normalFont;
        org.apache.pdfbox.pdmodel.font.PDFont smallFont;
        
        if ("ru".equals(lang)) {
          // Use TrueType font for Cyrillic support
          try {
            // Try to load Arial from Windows font directory
            String windowsFontPath = System.getenv("WINDIR") + "\\Fonts\\arial.ttf";
            java.io.File arialFile = new java.io.File(windowsFontPath);
            if (arialFile.exists()) {
              titleFont = PDType0Font.load(document, arialFile);
              headerFont = PDType0Font.load(document, arialFile);
              normalFont = PDType0Font.load(document, arialFile);
              smallFont = PDType0Font.load(document, arialFile);
              System.out.println("Loaded Arial font for Cyrillic support from: " + windowsFontPath);
            } else {
              throw new IOException("Arial font not found at: " + windowsFontPath);
            }
          } catch (Exception e) {
            // Fallback to standard fonts (will transliterate Cyrillic)
            System.err.println("Could not load Arial font, using standard fonts: " + e.getMessage());
            titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            smallFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
          }
        } else {
          // Use standard fonts for Turkish and English
          titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
          headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
          normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
          smallFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        }

        // Load logo - try multiple paths
        PDImageXObject logoImage = null;
        float logoHeight = 0;
        float logoWidth = 0;
        try {
          String[] logoFiles = {
            "logos/east contech.jpg",
            "logos/east contech 2.jpg", 
            "logos/east contech 3.jpg",
            "east contech.jpg",
            "east contech 2.jpg",
            "east contech 3.jpg",
            "/logos/east contech.jpg",
            "/logos/east contech 2.jpg",
            "/logos/east contech 3.jpg"
          };
          
          java.io.InputStream logoStream = null;
          String foundLogo = null;
          for (String logoFile : logoFiles) {
            logoStream = getClass().getClassLoader().getResourceAsStream(logoFile);
            if (logoStream != null) {
              foundLogo = logoFile;
              System.out.println("Logo found at: " + logoFile);
              break;
            }
            // Try without leading slash
            if (logoFile.startsWith("/")) {
              logoStream = getClass().getClassLoader().getResourceAsStream(logoFile.substring(1));
              if (logoStream != null) {
                foundLogo = logoFile.substring(1);
                System.out.println("Logo found at: " + foundLogo);
                break;
              }
            }
          }
          
          // Also try direct file path from frontend
          if (logoStream == null) {
            try {
              java.nio.file.Path frontendLogoPath = java.nio.file.Paths.get("frontend/public/logos/east contech.jpg");
              if (java.nio.file.Files.exists(frontendLogoPath)) {
                logoStream = java.nio.file.Files.newInputStream(frontendLogoPath);
                foundLogo = frontendLogoPath.toString();
                System.out.println("Logo found at file path: " + foundLogo);
              }
            } catch (Exception e) {
              System.err.println("Could not load logo from file path: " + e.getMessage());
            }
          }
          
          if (logoStream != null) {
            byte[] logoBytes = logoStream.readAllBytes();
            logoStream.close();
            logoImage = PDImageXObject.createFromByteArray(document, logoBytes, "logo");
            // Logo should be larger and prominent, matching title height
            float maxLogoHeight = 80; // Increased to match title better
            float maxLogoWidth = 300; // Increased width
            float logoScale = Math.min(maxLogoWidth / logoImage.getWidth(), maxLogoHeight / logoImage.getHeight());
            logoWidth = logoImage.getWidth() * logoScale;
            logoHeight = logoImage.getHeight() * logoScale;
            System.out.println("Logo loaded successfully: " + foundLogo + " (size: " + logoWidth + "x" + logoHeight + ")");
          } else {
            System.err.println("Logo not found. Tried: " + String.join(", ", logoFiles));
            System.err.println("Current working directory: " + System.getProperty("user.dir"));
            System.err.println("Classpath resources: " + getClass().getClassLoader().getResource("logos"));
          }
        } catch (Exception e) {
          System.err.println("Error loading logo: " + e.getMessage());
          e.printStackTrace();
        }

        // Draw logo and title aligned at the same height
        float headerY = pageHeight - margin - 20; // Common Y position for both logo and title
        
        if (logoImage != null) {
          try {
            // Logo in top left corner, aligned with title
            float logoX = margin;
            // Center logo vertically with title (title is 16pt font, approximately 20pt height)
            // Logo center should align with title center
            float titleTextHeight = 16; // Font size
            float logoCenterY = headerY - (titleTextHeight / 2f); // Center of title text
            float logoY = logoCenterY - (logoHeight / 2f); // Bottom of logo to center it
            contentStreamRef[0].drawImage(logoImage, logoX, logoY, logoWidth, logoHeight);
            System.out.println("Logo drawn at top left: " + logoX + ", " + logoY + " size: " + logoWidth + "x" + logoHeight);
          } catch (Exception e) {
            System.err.println("Error drawing logo: " + e.getMessage());
            e.printStackTrace();
          }
        } else {
          System.err.println("Logo image is null - cannot draw");
        }
        
        // Draw title with primary color - right aligned, same Y as logo center
        String title = asciiSafe(AccidentPdfTranslations.translate(lang, "title"), lang);
        float titleWidth = titleFont.getStringWidth(title) / 1000f * 16;
        float titleY = headerY; // Same Y position as logo center
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(titleFont, 16);
        contentStreamRef[0].setNonStrokingColor(0.176f, 0.749f, 0.506f); // #2DBF81 primary green
        contentStreamRef[0].newLineAtOffset(pageWidth - margin - titleWidth, titleY);
        contentStreamRef[0].showText(title);
        contentStreamRef[0].endText();
        contentStreamRef[0].setNonStrokingColor(0, 0, 0); // Reset to black
        
        // Move down from header
        currentY = pageHeight - margin - Math.max(logoHeight, 30) - 15;

        // Main information table - Multi-column format according to sample report
        // Parse formDataJson to get projectName and accidentClassification
        JsonNode formData = null;
        String projectName = "-";
        String accidentClassification = "-";
        String injuredPersonName = "-";
        String accidentTypeCode = "-";
        try {
          if (accident.formDataJson() != null && !accident.formDataJson().isBlank()) {
            formData = objectMapper.readTree(accident.formDataJson());
            if (formData.has("projectName") && !formData.get("projectName").isNull()) {
              projectName = formData.get("projectName").asText();
            }
            if (formData.has("accidentClassification") && !formData.get("accidentClassification").isNull()) {
              accidentClassification = formData.get("accidentClassification").asText();
            }
            if (formData.has("injuredPersonName") && !formData.get("injuredPersonName").isNull()) {
              injuredPersonName = formData.get("injuredPersonName").asText();
            }
            if (formData.has("accidentTypeCode") && !formData.get("accidentTypeCode").isNull()) {
              accidentTypeCode = formData.get("accidentTypeCode").asText();
            }
          }
        } catch (Exception e) {
          // Ignore parsing errors
        }
        // FormData'da ad soyad yerine UUID kalmışsa (eski kayıtlar): yaralı listesinden isim kullan
        if (injuredPersonName != null && injuredPersonName.length() >= 20 && injuredPersonName.contains("-")
            && !injured.isEmpty() && injured.get(0).getEmployee() != null) {
          var emp = injured.get(0).getEmployee();
          String first = emp.getFirstName() != null ? emp.getFirstName() : "";
          String last = emp.getLastName() != null ? emp.getLastName() : "";
          injuredPersonName = (first + " " + last).trim();
          if (injuredPersonName.isEmpty()) injuredPersonName = "-";
        }
        
        // Format date and time separately
        String dateStr = "-";
        String timeStr = "-";
        if (accident.occurredAt() != null) {
          String fullDateTime = accident.occurredAt().atZone(ZoneId.systemDefault())
              .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
          String[] parts = fullDateTime.split(" ");
          if (parts.length == 2) {
            dateStr = parts[0];
            timeStr = parts[1];
          } else {
            dateStr = fullDateTime;
          }
        }
        
        // Work related checkbox display
        String workRelatedDisplay = (accident.workRelated() != null && accident.workRelated()) 
            ? "☑ İşle İlgili ☐ İşle İlgisiz" 
            : "☐ İşle İlgili ☑ İşle İlgisiz";
        if ("en".equals(lang)) {
          workRelatedDisplay = (accident.workRelated() != null && accident.workRelated())
              ? "☑ Work Related ☐ Not Work Related"
              : "☐ Work Related ☑ Not Work Related";
        } else if ("ru".equals(lang)) {
          workRelatedDisplay = (accident.workRelated() != null && accident.workRelated())
              ? "☑ Связано с работой ☐ Не связано с работой"
              : "☐ Связано с работой ☑ Не связано с работой";
        }
        
        // Get injury type and body part translations
        String injuryTypeDisplay = "-";
        if (accident.injuryType() != null) {
          injuryTypeDisplay = AccidentPdfTranslations.translateInjuryType(lang, accident.injuryType());
        }
        String bodyPartDisplay = "-";
        if (accident.injuredBodyPart() != null) {
          bodyPartDisplay = AccidentPdfTranslations.translateBodyPart(lang, accident.injuredBodyPart());
        }
        
        // Draw multi-column table (includes all main information including work related and work during accident)
        currentY = drawMultiColumnTable(contentStreamRef, document, currentPageRef, margin, currentY, 
            contentWidth, lineHeight, headerFont, normalFont, pageHeight, bottomMargin, topMargin, lang,
            accident.groupCompanyName(), projectName, dateStr, timeStr, accident.location(), 
            accident.responsiblePerson(), accident.estimatedCost(), workRelatedDisplay,
            accident.workDuringAccident(), injuredPersonName, accidentTypeCode, 
            accident.injuredPersonAge(), accident.injuredPersonProfession(), 
            accident.injuredPersonGender(), accident.injuredPersonNationality(), 
            accidentClassification, accident.injuredPersonCompany(), injuryTypeDisplay, bodyPartDisplay);
        currentY -= 15;


        // 5. Kaza/Olay Açıklaması
        if (accident.description() != null && !accident.description().isBlank()) {
          String descTitle = "tr".equals(lang) ? "Kaza/Olay Açıklaması" : "en".equals(lang) ? "Accident/Event Description" : "Описание происшествия";
          
          currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 50, pageHeight, topMargin);
          
          contentStreamRef[0].beginText();
          contentStreamRef[0].setFont(headerFont, 11);
          contentStreamRef[0].newLineAtOffset(margin, currentY);
          contentStreamRef[0].showText(asciiSafe(descTitle, lang));
          contentStreamRef[0].endText();
          currentY -= 18;
          
          String description = asciiSafe(accident.description(), lang);
          String[] descLines = wrapText(description, 100);
          for (String line : descLines) {
            currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 20, pageHeight, topMargin);
            contentStreamRef[0].beginText();
            contentStreamRef[0].setFont(normalFont, 9);
            contentStreamRef[0].newLineAtOffset(margin, currentY);
            contentStreamRef[0].showText(line);
            contentStreamRef[0].endText();
            currentY -= lineHeight;
          }
          currentY -= 10;
        }

        // 6. Direkt Nedenler ve Kök Nedenler
        if (accident.rootCauseDataJson() != null && !accident.rootCauseDataJson().isBlank() && !accident.rootCauseDataJson().equals("{}")) {
          try {
            JsonNode rootCauseData = objectMapper.readTree(accident.rootCauseDataJson());
            
            // Direkt Nedenler
            currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 150, pageHeight, topMargin);
            String directCausesTitle = "tr".equals(lang) ? "Direkt Nedenler" : "en".equals(lang) ? "Direct Causes" : "Прямые причины";
              contentStreamRef[0].beginText();
              contentStreamRef[0].setFont(headerFont, 11);
              contentStreamRef[0].newLineAtOffset(margin, currentY);
            contentStreamRef[0].showText(asciiSafe(directCausesTitle, lang));
              contentStreamRef[0].endText();
            currentY -= 20;
            
            currentY = drawDirectCausesSection(contentStreamRef, document, currentPageRef, margin, currentY, rootCauseData, lang,
                pageHeight, bottomMargin, topMargin, headerFont, normalFont, lineHeight);
              currentY -= 15;
              
            // Kök Nedenler
            currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 150, pageHeight, topMargin);
            String rootCausesTitle = "tr".equals(lang) ? "Kök Nedenler" : "en".equals(lang) ? "Root Causes" : "Корневые причины";
              contentStreamRef[0].beginText();
            contentStreamRef[0].setFont(headerFont, 11);
              contentStreamRef[0].newLineAtOffset(margin, currentY);
            contentStreamRef[0].showText(asciiSafe(rootCausesTitle, lang));
              contentStreamRef[0].endText();
            currentY -= 20;
              
            currentY = drawRootCausesSection(contentStreamRef, document, currentPageRef, margin, currentY, rootCauseData, lang,
                pageHeight, bottomMargin, topMargin, headerFont, normalFont, lineHeight);
              currentY -= 15;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        
        // 7. Alınmış/Alınacak Aksiyonlar
        if (accident.actionsTakenJson() != null && !accident.actionsTakenJson().isBlank() && !accident.actionsTakenJson().equals("[]")) {
          try {
            JsonNode actionsArray = objectMapper.readTree(accident.actionsTakenJson());
            if (actionsArray.isArray() && actionsArray.size() > 0) {
              currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 100, pageHeight, topMargin);
              
              String actionsTitle = "tr".equals(lang) ? "Alınmış/Alınacak Aksiyonlar" : "en".equals(lang) ? "Actions Taken/To Be Taken" : "Принятые/Предстоящие действия";
              contentStreamRef[0].beginText();
              contentStreamRef[0].setFont(headerFont, 11);
              contentStreamRef[0].newLineAtOffset(margin, currentY);
              contentStreamRef[0].showText(asciiSafe(actionsTitle, lang));
              contentStreamRef[0].endText();
              currentY -= 20;
              
              currentY = drawActionsTable(contentStreamRef, document, currentPageRef, margin, currentY, actionsArray, lang,
                  pageHeight, bottomMargin, topMargin, headerFont, normalFont, lineHeight, contentWidth);
              currentY -= 15;
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        
        // 8. Raporu Hazırlayan ve Hazırlanma Tarihi
        if (accident.preparedBy() != null || accident.preparedAt() != null) {
          currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 50, pageHeight, topMargin);
          
          String preparedByLabel = "tr".equals(lang) ? "Raporu Hazırlayan" : "en".equals(lang) ? "Prepared By" : "Подготовил";
          String preparedAtLabel = "tr".equals(lang) ? "Hazırlanma Tarihi" : "en".equals(lang) ? "Preparation Date" : "Дата подготовки";
          
          String preparedByName = "-";
          if (accident.preparedBy() != null) {
            preparedByName = accident.preparedBy().firstName() + " " + accident.preparedBy().lastName();
          }
          
          String preparedAtStr = "-";
          if (accident.preparedAt() != null) {
            preparedAtStr = accident.preparedAt().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
          }
          
          List<TableRow> preparedRows = new ArrayList<>();
          preparedRows.add(new TableRow(asciiSafe(preparedByLabel, lang), asciiSafe(preparedByName, lang)));
          preparedRows.add(new TableRow(asciiSafe(preparedAtLabel, lang), preparedAtStr));
          
          currentY = drawTable(contentStreamRef, margin, currentY, preparedRows, contentWidth, lineHeight, headerFont, normalFont, pageHeight, bottomMargin, currentPageRef, document, lang);
          currentY -= 15;
        }

        // Photos/Attachments - Always show section
        List<FileObject> files = fileRepo.findByModuleAndEntityId(AccidentService.FILE_MODULE, accident.id());
        currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 50, pageHeight, topMargin);
        
        String photosTitle = asciiSafe(AccidentPdfTranslations.translate(lang, "photosFiles"), lang);
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(headerFont, 11);
        contentStreamRef[0].newLineAtOffset(margin, currentY);
        contentStreamRef[0].showText(photosTitle);
        contentStreamRef[0].endText();
        currentY -= 18;
        
        if (!files.isEmpty()) {
          System.out.println("Found " + files.size() + " files for accident " + accident.id());
          for (FileObject file : files) {
            if (file == null) continue;
            String contentType = file.getContentType();
            String storagePath = file.getStoragePath();
            String fileName = file.getOriginalFilename();
            
            System.out.println("Processing file: " + fileName + " (contentType: " + contentType + ", path: " + storagePath + ")");
            
            if (contentType != null && contentType.startsWith("image/")) {
              try {
                PDImageXObject image = null;
                
                System.out.println("=== IMAGE LOADING DEBUG ===");
                System.out.println("File: " + fileName);
                System.out.println("StoragePath from DB: " + storagePath);
                System.out.println("Module: " + (file != null ? file.getModule() : "null"));
                System.out.println("EntityId: " + (file != null ? file.getEntityId() : "null"));
                System.out.println("FileId: " + (file != null ? file.getId() : "null"));
                System.out.println("User.dir: " + System.getProperty("user.dir"));
                
                if (storagePath != null && !storagePath.isEmpty()) {
                  // storagePath is the full absolute path from FileStorageService.store()
                  // It should be something like: C:\Users\...\isc\storage\ACCIDENT\{entityId}\{fileId}
                  
                  // First, try the storagePath directly
                  try {
                    Path directPath = Path.of(storagePath);
                    Path normalizedPath = directPath.toAbsolutePath().normalize();
                    System.out.println("Trying direct path: " + normalizedPath);
                    System.out.println("Path exists: " + Files.exists(normalizedPath));
                    System.out.println("Is regular file: " + (Files.exists(normalizedPath) ? Files.isRegularFile(normalizedPath) : "N/A"));
                    
                    if (Files.exists(normalizedPath) && Files.isRegularFile(normalizedPath)) {
                      System.out.println("File found at direct path: " + normalizedPath);
                      System.out.println("File size: " + Files.size(normalizedPath) + " bytes");
                      try {
                        // Load image using BufferedImage (works without file extension)
                        BufferedImage bufferedImage = ImageIO.read(normalizedPath.toFile());
                        if (bufferedImage != null) {
                          // Convert BufferedImage to PDImageXObject
                          ByteArrayOutputStream baos = new ByteArrayOutputStream();
                          String format = contentType.contains("png") ? "PNG" : contentType.contains("jpeg") || contentType.contains("jpg") ? "JPEG" : "PNG";
                          ImageIO.write(bufferedImage, format, baos);
                          image = PDImageXObject.createFromByteArray(document, baos.toByteArray(), fileName != null ? fileName : "image");
                          if (image != null) {
                            System.out.println("Image loaded successfully from direct path! Dimensions: " + image.getWidth() + "x" + image.getHeight());
                          } else {
                            System.err.println("PDImageXObject.createFromByteArray returned null");
                          }
                        } else {
                          System.err.println("ImageIO.read returned null");
                        }
                      } catch (Exception imgEx) {
                        System.err.println("Failed to create PDImageXObject from file: " + imgEx.getMessage());
                        imgEx.printStackTrace();
                      }
                    }
                  } catch (Exception e) {
                    System.err.println("Direct path failed: " + e.getMessage());
                    e.printStackTrace();
                  }
                  
                  // If direct path failed, try constructing from module/entityId/fileId
                  if (image == null && file != null) {
                    try {
                      String module = file.getModule();
                      UUID entityId = file.getEntityId();
                      UUID fileId = file.getId();
                      
                      if (module != null && entityId != null && fileId != null) {
                        // Use the same logic as FileStorageService
                        // Get storage root from config (default: ./storage)
                        Path root = Path.of(storageProperties.storageRoot()).toAbsolutePath().normalize();
                        Path target = root.resolve(module).resolve(entityId.toString()).resolve(fileId.toString()).normalize();
                        
                        System.out.println("Trying constructed path: " + target);
                        System.out.println("Path exists: " + Files.exists(target));
                        System.out.println("Is regular file: " + (Files.exists(target) ? Files.isRegularFile(target) : "N/A"));
                        
                        if (Files.exists(target) && Files.isRegularFile(target)) {
                          System.out.println("File found at constructed path: " + target);
                          System.out.println("File size: " + Files.size(target) + " bytes");
                          try {
                            // Load image using BufferedImage (works without file extension)
                            BufferedImage bufferedImage = ImageIO.read(target.toFile());
                            if (bufferedImage != null) {
                              // Convert BufferedImage to PDImageXObject
                              ByteArrayOutputStream baos = new ByteArrayOutputStream();
                              String format = contentType.contains("png") ? "PNG" : contentType.contains("jpeg") || contentType.contains("jpg") ? "JPEG" : "PNG";
                              ImageIO.write(bufferedImage, format, baos);
                              image = PDImageXObject.createFromByteArray(document, baos.toByteArray(), fileName != null ? fileName : "image");
                              if (image != null) {
                                System.out.println("Image loaded successfully from constructed path! Dimensions: " + image.getWidth() + "x" + image.getHeight());
                              } else {
                                System.err.println("PDImageXObject.createFromByteArray returned null");
                              }
                            } else {
                              System.err.println("ImageIO.read returned null");
                            }
                          } catch (Exception imgEx) {
                            System.err.println("Failed to create PDImageXObject from file: " + imgEx.getMessage());
                            imgEx.printStackTrace();
                          }
                        }
                      }
                    } catch (Exception e) {
                      System.err.println("Constructed path failed: " + e.getMessage());
                      e.printStackTrace();
                    }
                  }
                  
                  // Last resort: try alternative path formats
                  if (image == null) {
                    try {
                      // Try with forward slashes converted to backslashes (Windows)
                      String normalizedStoragePath = storagePath.replace('/', java.io.File.separatorChar);
                      Path altPath = Path.of(normalizedStoragePath).toAbsolutePath().normalize();
                      System.out.println("Trying normalized path: " + altPath);
                      
                      if (Files.exists(altPath) && Files.isRegularFile(altPath)) {
                        System.out.println("File found at normalized path: " + altPath);
                        System.out.println("File size: " + Files.size(altPath) + " bytes");
                        try {
                          // Load image using BufferedImage (works without file extension)
                          BufferedImage bufferedImage = ImageIO.read(altPath.toFile());
                          if (bufferedImage != null) {
                            // Convert BufferedImage to PDImageXObject
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            String format = contentType.contains("png") ? "PNG" : contentType.contains("jpeg") || contentType.contains("jpg") ? "JPEG" : "PNG";
                            ImageIO.write(bufferedImage, format, baos);
                            image = PDImageXObject.createFromByteArray(document, baos.toByteArray(), fileName != null ? fileName : "image");
                            if (image != null) {
                              System.out.println("Image loaded successfully from normalized path! Dimensions: " + image.getWidth() + "x" + image.getHeight());
                            } else {
                              System.err.println("PDImageXObject.createFromByteArray returned null");
                            }
                          } else {
                            System.err.println("ImageIO.read returned null");
                          }
                        } catch (Exception imgEx) {
                          System.err.println("Failed to create PDImageXObject from file: " + imgEx.getMessage());
                          imgEx.printStackTrace();
                        }
                      }
                    } catch (Exception e) {
                      System.err.println("Normalized path failed: " + e.getMessage());
                    }
                  }
                }
                
                System.out.println("=== END IMAGE LOADING DEBUG ===");
                
                if (image != null) {
                  float imageWidth = image.getWidth();
                  float imageHeight = image.getHeight();
                  float maxWidth = contentWidth;
                  float maxHeight = 250; // Increased height for better image display
                  
                  float scale = Math.min(maxWidth / imageWidth, maxHeight / imageHeight);
                  float scaledWidth = imageWidth * scale;
                  float scaledHeight = imageHeight * scale;

                  currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + scaledHeight + 50, pageHeight, topMargin);
                  
                  // Draw image directly
                  contentStreamRef[0].drawImage(image, margin, currentY - scaledHeight, scaledWidth, scaledHeight);
                  System.out.println("Image drawn successfully at Y: " + (currentY - scaledHeight) + " size: " + scaledWidth + "x" + scaledHeight);
                  
                  // Show filename below image (optional)
                  if (fileName != null && !fileName.isEmpty()) {
                    contentStreamRef[0].beginText();
                    contentStreamRef[0].setFont(smallFont, 7);
                    contentStreamRef[0].newLineAtOffset(margin, currentY - scaledHeight - 12);
                    contentStreamRef[0].showText(asciiSafe(fileName, lang));
                    contentStreamRef[0].endText();
                  }
                  
                  currentY -= scaledHeight + 25; // More spacing after image
                } else {
                  System.err.println("Image file not found at any path for: " + fileName + " (storagePath: " + storagePath + ")");
                  
                  // If still not found, show error message
                  currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 20, pageHeight, topMargin);
                  if (fileName != null && !fileName.isEmpty()) {
                    contentStreamRef[0].beginText();
                    contentStreamRef[0].setFont(normalFont, 9);
                    contentStreamRef[0].newLineAtOffset(margin, currentY);
                    contentStreamRef[0].showText(asciiSafe(AccidentPdfTranslations.translate(lang, "file"), lang) + " " + asciiSafe(fileName, lang) + " " + asciiSafe(AccidentPdfTranslations.translate(lang, "fileNotLoaded"), lang));
                    contentStreamRef[0].endText();
                    currentY -= lineHeight;
                  }
                }
              } catch (Exception e) {
                System.err.println("Error loading image " + fileName + ": " + e.getMessage());
                e.printStackTrace();
                // Show filename even on error
                currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 20, pageHeight, topMargin);
                if (fileName != null && !fileName.isEmpty()) {
                  contentStreamRef[0].beginText();
                  contentStreamRef[0].setFont(normalFont, 9);
                  contentStreamRef[0].newLineAtOffset(margin, currentY);
                  contentStreamRef[0].showText(asciiSafe(AccidentPdfTranslations.translate(lang, "file")) + " " + asciiSafe(fileName) + " " + asciiSafe(AccidentPdfTranslations.translate(lang, "fileNotLoaded")));
                  contentStreamRef[0].endText();
                  currentY -= lineHeight;
                }
              }
            } else {
              currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 20, pageHeight, topMargin);
              if (fileName != null && !fileName.isEmpty()) {
                contentStreamRef[0].beginText();
                contentStreamRef[0].setFont(normalFont, 9);
                contentStreamRef[0].newLineAtOffset(margin, currentY);
                contentStreamRef[0].showText(asciiSafe(AccidentPdfTranslations.translate(lang, "file"), lang) + " " + asciiSafe(fileName, lang));
                contentStreamRef[0].endText();
                currentY -= lineHeight;
              }
            }
          }
        } else {
          // Show empty message if no files
          contentStreamRef[0].beginText();
          contentStreamRef[0].setFont(normalFont, 9);
          contentStreamRef[0].newLineAtOffset(margin, currentY);
          contentStreamRef[0].showText(asciiSafe("-", lang));
          contentStreamRef[0].endText();
          currentY -= lineHeight;
        }

        // Draw footer on all pages
        drawFooterOnAllPages(document, lang, margin, pageWidth, pageHeight, bottomMargin);
      } finally {
        if (contentStreamRef[0] != null) {
          try {
            contentStreamRef[0].endText();
          } catch (Exception e) {
            // Ignore
          }
          contentStreamRef[0].close();
        }
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      document.save(baos);
      return baos.toByteArray();
    }
  }

  private float drawTable(PDPageContentStream[] contentStreamRef, float margin, float y, List<TableRow> rows, 
      float tableWidth, float lineHeight, org.apache.pdfbox.pdmodel.font.PDFont labelFont, org.apache.pdfbox.pdmodel.font.PDFont valueFont,
      float pageHeight, float bottomMargin, PDPage[] currentPageRef, PDDocument document, String lang) throws IOException {
    
    // Adjust column width based on language
    float labelColWidth = "ru".equals(lang) ? 140f : 100f; // Wider for Russian
    float valueColWidth = tableWidth - labelColWidth; // Remaining for values
    float cellPadding = 8f; // Increased padding to prevent text from touching borders
    float minRowHeight = lineHeight + cellPadding * 2; // Minimum row height
    float currentY = y;
    
    // Draw table top border FIRST
    contentStreamRef[0].setLineWidth(1.5f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f); // #2DBF81 primary green
    contentStreamRef[0].moveTo(margin, currentY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    float tableStartY = currentY;
    
    for (int i = 0; i < rows.size(); i++) {
      TableRow row = rows.get(i);
      
      // Calculate text lines FIRST to determine row height
      // Adjust character width based on language (Cyrillic characters are wider)
      float charWidth = "ru".equals(lang) ? 5.5f : 4.5f; // Wider for Russian/Cyrillic
      int labelCharsPerLine = (int)((labelColWidth - cellPadding * 2) / charWidth);
      int valueCharsPerLine = (int)((valueColWidth - cellPadding * 2) / charWidth);
      String[] labelLines = wrapText(row.label, labelCharsPerLine);
      String[] valueLines = wrapText(row.value, valueCharsPerLine);
      
      // Calculate row height based on max lines
      int maxLines = Math.max(labelLines.length, valueLines.length);
      float rowHeight = Math.max(maxLines * lineHeight + cellPadding * 2, minRowHeight);
      
      // Check if we need a new page BEFORE drawing
      if (currentY - rowHeight < bottomMargin + 30) {
        // Close current page borders
        contentStreamRef[0].setLineWidth(1f);
        contentStreamRef[0].moveTo(margin, currentY);
        contentStreamRef[0].lineTo(margin + tableWidth, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].moveTo(margin, tableStartY);
        contentStreamRef[0].lineTo(margin, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].moveTo(margin + labelColWidth, tableStartY);
        contentStreamRef[0].lineTo(margin + labelColWidth, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].moveTo(margin + tableWidth, tableStartY);
        contentStreamRef[0].lineTo(margin + tableWidth, currentY);
        contentStreamRef[0].stroke();
        
        // New page
        contentStreamRef[0].close();
        PDPage newPage = new PDPage(PDRectangle.A4);
        document.addPage(newPage);
        currentPageRef[0] = newPage;
        contentStreamRef[0] = new PDPageContentStream(document, newPage);
        currentY = pageHeight - 50;
        tableStartY = currentY;
        
        // Redraw top border
        contentStreamRef[0].setLineWidth(1.5f);
        contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
        contentStreamRef[0].moveTo(margin, currentY);
        contentStreamRef[0].lineTo(margin + tableWidth, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].setStrokingColor(0, 0, 0);
      }
      
      // Draw cell borders FIRST (before text)
      float rowBottomY = currentY - rowHeight;
      
      // Draw vertical line between columns
      contentStreamRef[0].setLineWidth(0.5f);
      contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
      contentStreamRef[0].moveTo(margin + labelColWidth, currentY);
      contentStreamRef[0].lineTo(margin + labelColWidth, rowBottomY);
      contentStreamRef[0].stroke();
      
      // Draw horizontal line at bottom of row
      contentStreamRef[0].moveTo(margin, rowBottomY);
      contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
      contentStreamRef[0].stroke();
      contentStreamRef[0].setStrokingColor(0, 0, 0);
      
      // Draw label text - vertically centered within cell
      float totalTextHeight = labelLines.length * lineHeight;
      float cellCenterY = currentY - (rowHeight / 2f);
      float textStartY = cellCenterY + (totalTextHeight / 2f) - lineHeight; // Start from top of text block, centered
      
      for (String labelLine : labelLines) {
        if (labelLine != null && !labelLine.trim().isEmpty()) {
          contentStreamRef[0].beginText();
          contentStreamRef[0].setFont(labelFont, 10);
          contentStreamRef[0].newLineAtOffset(margin + cellPadding, textStartY);
          contentStreamRef[0].showText(labelLine);
          contentStreamRef[0].endText();
        }
        textStartY -= lineHeight;
      }
      
      // Draw value text - vertically centered, same logic as label
      float totalValueTextHeight = valueLines.length * lineHeight;
      float valueCellCenterY = currentY - (rowHeight / 2f);
      float valueTextStartY = valueCellCenterY + (totalValueTextHeight / 2f) - lineHeight;
      
      for (String valueLine : valueLines) {
        if (valueLine != null && !valueLine.trim().isEmpty()) {
          contentStreamRef[0].beginText();
          contentStreamRef[0].setFont(valueFont, 10);
          contentStreamRef[0].newLineAtOffset(margin + labelColWidth + cellPadding, valueTextStartY);
          contentStreamRef[0].showText(valueLine);
          contentStreamRef[0].endText();
        }
        valueTextStartY -= lineHeight;
      }
      
      // Move to next row
      currentY = rowBottomY;
    }
    
    // Draw final borders
    contentStreamRef[0].setLineWidth(1.5f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, tableStartY);
    contentStreamRef[0].lineTo(margin, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + tableWidth, tableStartY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    return currentY;
  }

  private float checkAndNewPage(PDPageContentStream[] contentStreamRef, PDDocument document, 
      PDPage[] currentPageRef, float currentY, float minSpace, float pageHeight, float topMargin) throws IOException {
    if (currentY < minSpace) {
      try {
        contentStreamRef[0].endText();
      } catch (Exception e) {
        // Ignore
      }
      contentStreamRef[0].close();
      PDPage newPage = new PDPage(PDRectangle.A4);
      document.addPage(newPage);
      currentPageRef[0] = newPage;
      contentStreamRef[0] = new PDPageContentStream(document, newPage);
      return pageHeight - topMargin;
    }
    return currentY;
  }

  private void drawFooterOnAllPages(PDDocument document, String lang, float margin, 
      float pageWidth, float pageHeight, float bottomMargin) throws IOException {
    String createdDate = java.time.LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    if ("en".equals(lang)) {
      createdDate = java.time.LocalDateTime.now()
          .format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
    }
    
    String createdLabel = "Olusturulma Tarihi:";
    String pageLabel = "Sayfa";
    if ("en".equals(lang)) {
      createdLabel = "Created Date:";
      pageLabel = "Page";
    } else if ("ru".equals(lang)) {
      createdLabel = "Data sozdaniya:";
      pageLabel = "Stranitsa";
    }
    
    int totalPages = document.getNumberOfPages();
    PDType1Font smallFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    
    for (int i = 0; i < totalPages; i++) {
      PDPage page = document.getPage(i);
      PDPageContentStream footerStream = new PDPageContentStream(document, page, 
          PDPageContentStream.AppendMode.APPEND, true, true);
      
      try {
        footerStream.setLineWidth(0.5f);
        footerStream.moveTo(margin, bottomMargin + 10);
        footerStream.lineTo(pageWidth - margin, bottomMargin + 10);
        footerStream.stroke();
        
        footerStream.beginText();
        footerStream.setFont(smallFont, 7);
        footerStream.newLineAtOffset(margin, bottomMargin);
        footerStream.showText(asciiSafe(createdLabel + " " + createdDate, lang));
        footerStream.endText();
        
        String pageText = pageLabel + " " + (i + 1) + " / " + totalPages;
        float pageTextWidth = smallFont.getStringWidth(pageText) / 1000f * 7;
        footerStream.beginText();
        footerStream.setFont(smallFont, 7);
        footerStream.newLineAtOffset(pageWidth - margin - pageTextWidth, bottomMargin);
        footerStream.showText(asciiSafe(pageText, lang));
        footerStream.endText();
      } finally {
        footerStream.close();
      }
    }
  }

  private String[] wrapText(String text, int maxLength) {
    if (text == null || text.isEmpty()) return new String[] { "-" };
    if (maxLength <= 0) maxLength = 50;
    
    text = text.trim().replaceAll("\\s+", " ");
    
    if (text.length() <= maxLength) return new String[] { text };
    
    List<String> lines = new ArrayList<>();
    int start = 0;
    
    while (start < text.length()) {
      int end = Math.min(start + maxLength, text.length());
      
      if (end < text.length()) {
        int lastSpace = text.lastIndexOf(' ', end);
        int lastComma = text.lastIndexOf(',', end);
        int lastPeriod = text.lastIndexOf('.', end);
        
        int breakPoint = Math.max(Math.max(lastSpace, lastComma), lastPeriod);
        
        if (breakPoint > start + maxLength * 0.6) {
          end = breakPoint + 1;
        }
      }
      
      String line = text.substring(start, end).trim();
      if (!line.isEmpty()) {
        lines.add(line);
      }
      
      start = end;
      while (start < text.length() && Character.isWhitespace(text.charAt(start))) {
        start++;
      }
    }
    
    return lines.isEmpty() ? new String[] { "-" } : lines.toArray(new String[0]);
  }

  private String formatTimePeriod(String value) {
    if (value == null || value.isEmpty()) return "-";
    String m = value.replace("*", " - ");
    if (m.matches("\\d{4} - \\d{4}")) {
      return m.substring(0, 2) + ":" + m.substring(2, 4) + " - " + m.substring(7, 9) + ":" + m.substring(9, 11);
    }
    return m;
  }

  private static class CauseItem {
    final String name;
    final int count;
    
    CauseItem(String name, int count) {
      this.name = name;
      this.count = count;
    }
  }
  
  // Helper function to draw table and pie chart
  private float drawTableAndPieChart(PDPageContentStream[] contentStreamRef, PDDocument document,
      PDPage[] currentPageRef, float margin, float y, List<CauseItem> causes, String lang,
      float pageHeight, float bottomMargin, float topMargin, org.apache.pdfbox.pdmodel.font.PDFont headerFont, 
      org.apache.pdfbox.pdmodel.font.PDFont normalFont, float lineHeight, float pageWidth, String tableTitle) throws IOException {
    
    float currentY = y;
    float tableWidth = (pageWidth - 3 * margin) / 2; // Half width for table
    float chartX = margin + tableWidth + margin; // Right side for chart
    float chartY = currentY;
    float chartSize = tableWidth * 0.8f; // Chart size
    float chartCenterX = chartX + tableWidth / 2;
    float chartCenterY = chartY - chartSize / 2;
    
    // Draw table on left
    float tableRowHeight = 12f;
    float tableHeaderHeight = 15f;
    
    // Table header
    contentStreamRef[0].setLineWidth(1f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    // Header text
    String noLabel = "No";
    contentStreamRef[0].beginText();
    contentStreamRef[0].setFont(headerFont, 9);
    contentStreamRef[0].newLineAtOffset(margin + 5, currentY - 10);
    contentStreamRef[0].showText(asciiSafe(noLabel, lang));
    contentStreamRef[0].endText();
    
    contentStreamRef[0].beginText();
    contentStreamRef[0].setFont(headerFont, 9);
    contentStreamRef[0].newLineAtOffset(margin + 30, currentY - 10);
    contentStreamRef[0].showText(asciiSafe(tableTitle, lang));
    contentStreamRef[0].endText();
    
    currentY -= tableHeaderHeight;
    
    // Draw table rows
    int rowNum = 1;
    for (CauseItem cause : causes) {
      if (currentY - tableRowHeight < bottomMargin + 50) {
        currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 100, pageHeight, topMargin);
        chartY = currentY;
        chartCenterY = chartY - chartSize / 2;
      }
      
      // Row border
      contentStreamRef[0].setLineWidth(0.5f);
      contentStreamRef[0].moveTo(margin, currentY);
      contentStreamRef[0].lineTo(margin + tableWidth, currentY);
      contentStreamRef[0].stroke();
      
      // Row number
      contentStreamRef[0].beginText();
      contentStreamRef[0].setFont(normalFont, 8);
      contentStreamRef[0].newLineAtOffset(margin + 5, currentY - 8);
      contentStreamRef[0].showText(String.valueOf(rowNum));
      contentStreamRef[0].endText();
      
      // Cause name
      String causeName = cause.name;
      if (causeName.length() > 35) {
        causeName = causeName.substring(0, 32) + "...";
      }
      contentStreamRef[0].beginText();
      contentStreamRef[0].setFont(normalFont, 8);
      contentStreamRef[0].newLineAtOffset(margin + 30, currentY - 8);
      contentStreamRef[0].showText(asciiSafe(causeName, lang));
      contentStreamRef[0].endText();
      
      // Count
      contentStreamRef[0].beginText();
      contentStreamRef[0].setFont(normalFont, 8);
      float countWidth = normalFont.getStringWidth(String.valueOf(cause.count)) / 1000f * 8;
      contentStreamRef[0].newLineAtOffset(margin + tableWidth - countWidth - 5, currentY - 8);
      contentStreamRef[0].showText(String.valueOf(cause.count));
      contentStreamRef[0].endText();
      
      currentY -= tableRowHeight;
      rowNum++;
    }
    
    // Draw bottom border
    contentStreamRef[0].setLineWidth(1f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    // Draw pie chart on right
    if (!causes.isEmpty()) {
      int totalCount = causes.stream().mapToInt(c -> c.count).sum();
      float startAngle = (float)(-Math.PI / 2); // Start from top
      
      float[][] colors = {
        {0.8f, 0.2f, 0.2f}, {0.2f, 0.4f, 0.8f}, {1.0f, 0.6f, 0.0f}, {0.6f, 0.4f, 0.2f},
        {1.0f, 0.8f, 0.8f}, {0.4f, 0.2f, 0.1f}, {0.5f, 0.7f, 0.3f}, {0.9f, 0.5f, 0.7f},
        {0.3f, 0.6f, 0.9f}, {0.7f, 0.5f, 0.2f}
      };
      int colorIndex = 0;
      
      float radius = chartSize / 2.5f;
      
      for (CauseItem cause : causes) {
        float angle = (float)(2 * Math.PI * cause.count / totalCount);
        float endAngle = startAngle + angle;
        
        float[] color = colors[colorIndex % colors.length];
        contentStreamRef[0].setNonStrokingColor(color[0], color[1], color[2]);
        colorIndex++;
        
        contentStreamRef[0].moveTo(chartCenterX, chartCenterY);
        int segments = Math.max(20, (int)(angle * 10));
        for (int i = 0; i <= segments; i++) {
          float a = startAngle + (angle * i / segments);
          float xPos = chartCenterX + radius * (float)Math.cos(a);
          float yPos = chartCenterY + radius * (float)Math.sin(a);
          contentStreamRef[0].lineTo(xPos, yPos);
        }
        contentStreamRef[0].lineTo(chartCenterX, chartCenterY);
        contentStreamRef[0].closePath();
        contentStreamRef[0].fill();
        
        startAngle = endAngle;
      }
      
      contentStreamRef[0].setStrokingColor(0, 0, 0);
      contentStreamRef[0].setLineWidth(1f);
      drawCircle(contentStreamRef[0], chartCenterX, chartCenterY, radius);
      contentStreamRef[0].stroke();
    }
    
    return Math.min(currentY, chartY - chartSize - 10);
  }
  
  // Section 1: Personnel Factors (Personel Faktörleri)
  private float drawPersonnelFactorsTableAndChart(PDPageContentStream[] contentStreamRef, PDDocument document,
      PDPage[] currentPageRef, float margin, float y, JsonNode rootCauseData, String lang,
      float pageHeight, float bottomMargin, float topMargin, org.apache.pdfbox.pdmodel.font.PDFont headerFont, 
      org.apache.pdfbox.pdmodel.font.PDFont normalFont, float lineHeight, float pageWidth) throws IOException {
    
    List<CauseItem> causes = new ArrayList<>();
    JsonNode humanNode = rootCauseData.get("human");
    
    if (humanNode != null && !humanNode.isNull()) {
      List<String> items = new ArrayList<>();
      if (humanNode.isArray()) {
        for (JsonNode item : humanNode) {
          if (item.isTextual()) items.add(item.asText());
        }
      } else if (humanNode.isObject()) {
        var subEntries = new ArrayList<Map.Entry<String, JsonNode>>();
        humanNode.fields().forEachRemaining(subEntries::add);
        for (var subEntry : subEntries) {
          if (subEntry.getValue().asBoolean()) items.add(subEntry.getKey());
        }
      }
      
      Map<String, Integer> itemCounts = new HashMap<>();
      for (String item : items) {
        itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
      }
      
      for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
        String itemName = translateRootCauseSubCategory(lang, "human", entry.getKey());
        causes.add(new CauseItem(itemName, entry.getValue()));
      }
    }
    
    causes.sort((a, b) -> Integer.compare(b.count, a.count));
    String tableTitle = "tr".equals(lang) ? "Kok Sebep" : "en".equals(lang) ? "Root Cause" : "Корневая причина";
    return drawTableAndPieChart(contentStreamRef, document, currentPageRef, margin, y, causes, lang,
        pageHeight, bottomMargin, topMargin, headerFont, normalFont, lineHeight, pageWidth, tableTitle);
  }
  
  // Section 2: Root Causes (Kök Sebepler)
  private float drawRootCausesTableAndChart(PDPageContentStream[] contentStreamRef, PDDocument document,
      PDPage[] currentPageRef, float margin, float y, JsonNode rootCauseData, String lang,
      float pageHeight, float bottomMargin, float topMargin, org.apache.pdfbox.pdmodel.font.PDFont headerFont, 
      org.apache.pdfbox.pdmodel.font.PDFont normalFont, float lineHeight, float pageWidth) throws IOException {
    
    List<CauseItem> causes = new ArrayList<>();
    String[] categories = {"human", "equipment", "environment", "management"};
    
    for (String categoryKey : categories) {
      JsonNode categoryNode = rootCauseData.get(categoryKey);
      if (categoryNode == null || categoryNode.isNull()) continue;
      
      List<String> items = new ArrayList<>();
      if (categoryNode.isArray()) {
        for (JsonNode item : categoryNode) {
          if (item.isTextual()) items.add(item.asText());
        }
      } else if (categoryNode.isObject()) {
        var subEntries = new ArrayList<Map.Entry<String, JsonNode>>();
        categoryNode.fields().forEachRemaining(subEntries::add);
        for (var subEntry : subEntries) {
          if (subEntry.getValue().asBoolean()) items.add(subEntry.getKey());
        }
      }
      
      Map<String, Integer> itemCounts = new HashMap<>();
      for (String item : items) {
        itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
      }
      
      for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
        String itemName = translateRootCauseSubCategory(lang, categoryKey, entry.getKey());
        causes.add(new CauseItem(itemName, entry.getValue()));
      }
    }
    
    causes.sort((a, b) -> Integer.compare(b.count, a.count));
    String tableTitle = "tr".equals(lang) ? "Kok Sebep" : "en".equals(lang) ? "Root Cause" : "Корневая причина";
    return drawTableAndPieChart(contentStreamRef, document, currentPageRef, margin, y, causes, lang,
        pageHeight, bottomMargin, topMargin, headerFont, normalFont, lineHeight, pageWidth, tableTitle);
  }
  
  // Section 3: Immediate Causes (Doğrudan Sebepler)
  private float drawImmediateCausesTableAndChart(PDPageContentStream[] contentStreamRef, PDDocument document,
      PDPage[] currentPageRef, float margin, float y, JsonNode formData, JsonNode rootCauseData, String lang,
      float pageHeight, float bottomMargin, float topMargin, org.apache.pdfbox.pdmodel.font.PDFont headerFont, 
      org.apache.pdfbox.pdmodel.font.PDFont normalFont, float lineHeight, float pageWidth) throws IOException {
    
    // Use rootCauseData as fallback, or extract from formData if available
    List<CauseItem> causes = new ArrayList<>();
    String[] categories = {"human", "equipment", "environment", "management"};
    
    for (String categoryKey : categories) {
      JsonNode categoryNode = rootCauseData.get(categoryKey);
      if (categoryNode == null || categoryNode.isNull()) continue;
      
      List<String> items = new ArrayList<>();
      if (categoryNode.isArray()) {
        for (JsonNode item : categoryNode) {
          if (item.isTextual()) items.add(item.asText());
        }
      } else if (categoryNode.isObject()) {
        var subEntries = new ArrayList<Map.Entry<String, JsonNode>>();
        categoryNode.fields().forEachRemaining(subEntries::add);
        for (var subEntry : subEntries) {
          if (subEntry.getValue().asBoolean()) items.add(subEntry.getKey());
        }
      }
      
      Map<String, Integer> itemCounts = new HashMap<>();
      for (String item : items) {
        itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
      }
      
      for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
        String itemName = translateRootCauseSubCategory(lang, categoryKey, entry.getKey());
        causes.add(new CauseItem(itemName, entry.getValue()));
      }
    }
    
    causes.sort((a, b) -> Integer.compare(b.count, a.count));
    String tableTitle = "tr".equals(lang) ? "Dogrudan Sebep" : "en".equals(lang) ? "Immediate Cause" : "Непосредственная причина";
    return drawTableAndPieChart(contentStreamRef, document, currentPageRef, margin, y, causes, lang,
        pageHeight, bottomMargin, topMargin, headerFont, normalFont, lineHeight, pageWidth, tableTitle);
  }

  private float drawRootCauseDiagram(PDPageContentStream[] contentStreamRef, PDDocument document, 
      PDPage[] currentPageRef, float margin, float y, JsonNode rootCauseData, String lang,
      float pageHeight, float bottomMargin, float topMargin, org.apache.pdfbox.pdmodel.font.PDFont headerFont, 
      org.apache.pdfbox.pdmodel.font.PDFont normalFont, float lineHeight) throws IOException {
    
    float currentY = y;
    float pageWidth = pageHeight * 0.707f; // A4 ratio
    float centerX = margin + (pageWidth - 2 * margin) / 2; // Center of page width
    float nodeRadius = 15; // Smaller
    float categoryRadius = 12; // Smaller
    float itemRadius = 8; // Smaller
    
    // Center node
    String centerLabel = asciiSafe(AccidentPdfTranslations.translate(lang, "rootCauseAnalysis"), lang);
    float centerY = currentY - 30; // Positioned properly below header
    
    // Draw center node (circle using path)
    contentStreamRef[0].setNonStrokingColor(0.176f, 0.749f, 0.506f); // Green
    drawCircle(contentStreamRef[0], centerX, centerY, nodeRadius);
    contentStreamRef[0].fill();
    contentStreamRef[0].setNonStrokingColor(0, 0, 0);
    
    contentStreamRef[0].beginText();
    contentStreamRef[0].setFont(normalFont, 8);
    float centerTextWidth = normalFont.getStringWidth(centerLabel) / 1000f * 8;
    contentStreamRef[0].newLineAtOffset(centerX - centerTextWidth / 2, centerY - 3);
    contentStreamRef[0].showText(centerLabel);
    contentStreamRef[0].endText();
    
    // Categories around center - smaller scale
    String[] categories = {"human", "equipment", "environment", "management"};
    float angleStep = (float)(2 * Math.PI / categories.length);
    float categoryDistance = 60; // Smaller distance
    
    List<String> allItems = new ArrayList<>();
    
    for (int i = 0; i < categories.length; i++) {
      String categoryKey = categories[i];
      JsonNode categoryNode = rootCauseData.get(categoryKey);
      if (categoryNode == null || categoryNode.isNull()) continue;
      
      float angle = i * angleStep;
      float catX = centerX + (float)(categoryDistance * Math.cos(angle));
      float catY = centerY + (float)(categoryDistance * Math.sin(angle));
      
      String categoryName = translateRootCauseCategory(lang, categoryKey);
      
      // Draw category node
      contentStreamRef[0].setNonStrokingColor(0.906f, 0.965f, 0.929f); // Light green
      drawCircle(contentStreamRef[0], catX, catY, categoryRadius);
      contentStreamRef[0].fill();
      contentStreamRef[0].setNonStrokingColor(0, 0, 0);
      
      // Draw line from center to category
      contentStreamRef[0].setLineWidth(1f);
      contentStreamRef[0].moveTo(centerX, centerY);
      contentStreamRef[0].lineTo(catX, catY);
      contentStreamRef[0].stroke();
      
      // Category label
      contentStreamRef[0].beginText();
      contentStreamRef[0].setFont(normalFont, 7);
      float catTextWidth = normalFont.getStringWidth(categoryName) / 1000f * 7;
      contentStreamRef[0].newLineAtOffset(catX - catTextWidth / 2, catY - 2);
      contentStreamRef[0].showText(asciiSafe(categoryName, lang));
      contentStreamRef[0].endText();
      
      // Get items for this category
      List<String> items = new ArrayList<>();
      if (categoryNode.isArray()) {
        for (JsonNode item : categoryNode) {
          if (item.isTextual()) {
            items.add(item.asText());
          }
        }
      } else if (categoryNode.isObject()) {
        var subEntries = new ArrayList<Map.Entry<String, JsonNode>>();
        categoryNode.fields().forEachRemaining(subEntries::add);
        for (var subEntry : subEntries) {
          if (subEntry.getValue().asBoolean()) {
            items.add(subEntry.getKey());
          }
        }
      }
      
      // Draw items around category - smaller scale
      float itemDistance = 35; // Smaller distance
      
      for (int j = 0; j < items.size(); j++) {
        String itemKey = items.get(j);
        String itemName = translateRootCauseSubCategory(lang, categoryKey, itemKey);
        
        float itemAngle = angle + (j - items.size() / 2f) * 0.3f; // Spread items
        float itemX = catX + (float)(itemDistance * Math.cos(itemAngle));
        float itemY = catY + (float)(itemDistance * Math.sin(itemAngle));
        
        // Draw item node
        contentStreamRef[0].setNonStrokingColor(0.8f, 0.8f, 0.8f); // Gray
        drawCircle(contentStreamRef[0], itemX, itemY, itemRadius);
        contentStreamRef[0].fill();
        contentStreamRef[0].setNonStrokingColor(0, 0, 0);
        
        // Draw line from category to item
        contentStreamRef[0].setLineWidth(0.5f);
        contentStreamRef[0].moveTo(catX, catY);
        contentStreamRef[0].lineTo(itemX, itemY);
        contentStreamRef[0].stroke();
        
        // Item label (shortened)
        String shortName = itemName.length() > 15 ? itemName.substring(0, 12) + "..." : itemName;
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(normalFont, 6);
        float itemTextWidth = normalFont.getStringWidth(shortName) / 1000f * 6;
        contentStreamRef[0].newLineAtOffset(itemX - itemTextWidth / 2, itemY - 1);
        contentStreamRef[0].showText(asciiSafe(shortName, lang));
        contentStreamRef[0].endText();
      }
      
      allItems.addAll(items);
    }
    
    // Return new Y position - smaller diagram uses less space
    return currentY - 120; // Reduced space used
  }

  private void drawCircle(PDPageContentStream contentStream, float x, float y, float radius) throws IOException {
    // Draw circle using Bezier curves
    float k = 0.552284749831f; // Magic number for circle approximation
    contentStream.moveTo(x + radius, y);
    contentStream.curveTo(x + radius, y + k * radius, x + k * radius, y + radius, x, y + radius);
    contentStream.curveTo(x - k * radius, y + radius, x - radius, y + k * radius, x - radius, y);
    contentStream.curveTo(x - radius, y - k * radius, x - k * radius, y - radius, x, y - radius);
    contentStream.curveTo(x + k * radius, y - radius, x + radius, y - k * radius, x + radius, y);
    contentStream.closePath();
  }

  private String translateRootCauseCategory(String lang, String category) {
    Map<String, Map<String, String>> translations = Map.of(
      "tr", Map.of(
        "human", "Insan Faktoru",
        "equipment", "Ekipman",
        "environment", "Cevre",
        "management", "Yonetim"
      ),
      "en", Map.of(
        "human", "Human Factor",
        "equipment", "Equipment",
        "environment", "Environment",
        "management", "Management"
      ),
      "ru", Map.of(
        "human", "Человеческий фактор",
        "equipment", "Оборудование",
        "environment", "Окружающая среда",
        "management", "Управление"
      )
    );
    return translations.getOrDefault(lang, translations.get("tr")).getOrDefault(category, category);
  }

  private String translateRootCauseSubCategory(String lang, String category, String subCategory) {
    // Translate root cause options
    Map<String, String> trMap = new HashMap<>();
    trMap.put("CARELESSNESS", "Dikkatsizlik");
    trMap.put("INSUFFICIENT_TRAINING", "Yetersiz egitim");
    trMap.put("FATIGUE", "Yorgunluk");
    trMap.put("NON_COMPLIANCE", "Talimata uymama");
    trMap.put("WRONG_EQUIPMENT_USE", "Yanlis ekipman kullanimi");
    trMap.put("EQUIPMENT_FAILURE", "Ekipman arizasi");
    trMap.put("PROTECTIVE_LACK", "Koruyucu eksikligi");
    trMap.put("MAINTENANCE_INSUFFICIENCY", "Bakim yetersizligi");
    trMap.put("INAPPROPRIATE_EQUIPMENT", "Uygunsuz ekipman");
    trMap.put("FLOOR_INAPPROPRIATENESS", "Zemin uygunsuzlugu");
    trMap.put("LIGHTING_INSUFFICIENCY", "Aydinlatma yetersizligi");
    trMap.put("NOISE", "Gurultu");
    trMap.put("WEATHER_CONDITIONS", "Hava kosullari");
    trMap.put("ORDER_CLEANLINESS_LACK", "Duzen/temizlik eksikligi");
    trMap.put("AUDIT_INSUFFICIENCY", "Denetim yetersizligi");
    trMap.put("PROCEDURE_LACK", "Prosedur eksikligi");
    trMap.put("RISK_ASSESSMENT_LACK", "Risk degerlendirme eksikligi");
    trMap.put("WORKLOAD_PRESSURE", "Is yuku/baski");
    trMap.put("COMMUNICATION_LACK", "Iletisim eksikligi");
    
    Map<String, String> enMap = new HashMap<>();
    enMap.put("CARELESSNESS", "Carelessness");
    enMap.put("INSUFFICIENT_TRAINING", "Insufficient training");
    enMap.put("FATIGUE", "Fatigue");
    enMap.put("NON_COMPLIANCE", "Non-compliance");
    enMap.put("WRONG_EQUIPMENT_USE", "Wrong equipment use");
    enMap.put("EQUIPMENT_FAILURE", "Equipment failure");
    enMap.put("PROTECTIVE_LACK", "Protective lack");
    enMap.put("MAINTENANCE_INSUFFICIENCY", "Maintenance insufficiency");
    enMap.put("INAPPROPRIATE_EQUIPMENT", "Inappropriate equipment");
    enMap.put("FLOOR_INAPPROPRIATENESS", "Floor inappropriateness");
    enMap.put("LIGHTING_INSUFFICIENCY", "Lighting insufficiency");
    enMap.put("NOISE", "Noise");
    enMap.put("WEATHER_CONDITIONS", "Weather conditions");
    enMap.put("ORDER_CLEANLINESS_LACK", "Order/cleanliness lack");
    enMap.put("AUDIT_INSUFFICIENCY", "Audit insufficiency");
    enMap.put("PROCEDURE_LACK", "Procedure lack");
    enMap.put("RISK_ASSESSMENT_LACK", "Risk assessment lack");
    enMap.put("WORKLOAD_PRESSURE", "Workload pressure");
    enMap.put("COMMUNICATION_LACK", "Communication lack");
    
    Map<String, String> ruMap = new HashMap<>();
    ruMap.put("CARELESSNESS", "Небрежность");
    ruMap.put("INSUFFICIENT_TRAINING", "Недостаточное обучение");
    ruMap.put("FATIGUE", "Усталость");
    ruMap.put("NON_COMPLIANCE", "Несоответствие");
    ruMap.put("WRONG_EQUIPMENT_USE", "Неправильное использование оборудования");
    ruMap.put("EQUIPMENT_FAILURE", "Отказ оборудования");
    ruMap.put("PROTECTIVE_LACK", "Отсутствие защиты");
    ruMap.put("MAINTENANCE_INSUFFICIENCY", "Недостаточное обслуживание");
    ruMap.put("INAPPROPRIATE_EQUIPMENT", "Неподходящее оборудование");
    ruMap.put("FLOOR_INAPPROPRIATENESS", "Неподходящий пол");
    ruMap.put("LIGHTING_INSUFFICIENCY", "Недостаточное освещение");
    ruMap.put("NOISE", "Шум");
    ruMap.put("WEATHER_CONDITIONS", "Погодные условия");
    ruMap.put("ORDER_CLEANLINESS_LACK", "Отсутствие порядка/чистоты");
    ruMap.put("AUDIT_INSUFFICIENCY", "Недостаточный аудит");
    ruMap.put("PROCEDURE_LACK", "Отсутствие процедуры");
    ruMap.put("RISK_ASSESSMENT_LACK", "Отсутствие оценки риска");
    ruMap.put("WORKLOAD_PRESSURE", "Давление рабочей нагрузки");
    ruMap.put("COMMUNICATION_LACK", "Отсутствие коммуникации");
    
    Map<String, Map<String, String>> translations = new HashMap<>();
    translations.put("tr", trMap);
    translations.put("en", enMap);
    translations.put("ru", ruMap);
    
    String translated = translations.getOrDefault(lang, trMap).getOrDefault(subCategory, subCategory);
    return asciiSafe(translated, lang);
  }

  private String asciiSafe(String text, String lang) {
    if (text == null) return "";
    
    // Remove special characters that are not available in Helvetica font (WinAnsiEncoding)
    // Checkbox characters, other Unicode symbols, and № (U+2116 NUMERO SIGN)
    text = text.replace("\u2116", "No.").replace("\u2611", "[X]").replace("\u2610", "[ ]").replace("\u2713", "[V]")
        .replace("\u2022", "-").replace("\u2013", "-").replace("\u2014", "-")
        .replace("\u2018", "'").replace("\u2019", "'").replace("\u201C", "\"").replace("\u201D", "\"");
    
    // Helvetica uses WinAnsiEncoding: transliterate Turkish and Cyrillic for all languages
    String result = text
        .replace("İ", "I").replace("ı", "i")
        .replace("Ğ", "G").replace("ğ", "g")
        .replace("Ü", "U").replace("ü", "u")
        .replace("Ş", "S").replace("ş", "s")
        .replace("Ö", "O").replace("ö", "o")
        .replace("Ç", "C").replace("ç", "c");
    
    result = result
        .replace("А", "A").replace("а", "a")
        .replace("Б", "B").replace("б", "b")
        .replace("В", "V").replace("в", "v")
        .replace("Г", "G").replace("г", "g")
        .replace("Д", "D").replace("д", "d")
        .replace("Е", "E").replace("е", "e")
        .replace("Ё", "E").replace("ё", "e")
        .replace("Ж", "Zh").replace("ж", "zh")
        .replace("З", "Z").replace("з", "z")
        .replace("И", "I").replace("и", "i")
        .replace("Й", "Y").replace("й", "y")
        .replace("К", "K").replace("к", "k")
        .replace("Л", "L").replace("л", "l")
        .replace("М", "M").replace("м", "m")
        .replace("Н", "N").replace("н", "n")
        .replace("О", "O").replace("о", "o")
        .replace("П", "P").replace("п", "p")
        .replace("Р", "R").replace("р", "r")
        .replace("С", "S").replace("с", "s")
        .replace("Т", "T").replace("т", "t")
        .replace("У", "U").replace("у", "u")
        .replace("Ф", "F").replace("ф", "f")
        .replace("Х", "Kh").replace("х", "kh")
        .replace("Ц", "Ts").replace("ц", "ts")
        .replace("Ч", "Ch").replace("ч", "ch")
        .replace("Ш", "Sh").replace("ш", "sh")
        .replace("Щ", "Shch").replace("щ", "shch")
        .replace("Ъ", "").replace("ъ", "")
        .replace("Ы", "Y").replace("ы", "y")
        .replace("Ь", "").replace("ь", "")
        .replace("Э", "E").replace("э", "e")
        .replace("Ю", "Yu").replace("ю", "yu")
        .replace("Я", "Ya").replace("я", "ya");
    
    return result;
  }
  
  // Overload for backward compatibility
  private String asciiSafe(String text) {
    return asciiSafe(text, "tr"); // Default to Turkish
  }

  private static class TableRow {
    final String label;
    final String value;

    TableRow(String label, String value) {
      this.label = label;
      this.value = value;
    }
  }
  
  // Draw multi-column table for main information (like sample report)
  private float drawMultiColumnTable(PDPageContentStream[] contentStreamRef, PDDocument document,
      PDPage[] currentPageRef, float margin, float y, float tableWidth, float lineHeight,
      org.apache.pdfbox.pdmodel.font.PDFont headerFont, org.apache.pdfbox.pdmodel.font.PDFont normalFont,
      float pageHeight, float bottomMargin, float topMargin, String lang,
      String groupCompanyName, String projectName, String dateStr, String timeStr, String location,
      String responsiblePerson, String estimatedCost, String workRelatedDisplay,
      String workDuringAccident, String injuredPersonName, String accidentTypeCode,
      Integer injuredPersonAge, String injuredPersonProfession, String injuredPersonGender,
      String injuredPersonNationality, String accidentClassification, String injuredPersonCompany,
      String injuryTypeDisplay, String bodyPartDisplay) throws IOException {
    
    float[] currentYRef = new float[] { y };
    float cellPadding = 6f;
    float rowHeight = 20f;
    
    // Calculate pageWidth from pageHeight (A4 ratio)
    float pageWidth = pageHeight * 0.707f;
    
    // Column widths: 4 columns total
    float col1Width = tableWidth * 0.25f; // Label 1
    float col2Width = tableWidth * 0.25f; // Value 1
    float col3Width = tableWidth * 0.25f; // Label 2
    float col4Width = tableWidth * 0.25f; // Value 2
    
    // Calculate total rows needed
    int totalRows = 10; // Approximate rows needed
    currentYRef[0] = checkAndNewPage(contentStreamRef, document, currentPageRef, currentYRef[0], bottomMargin + (totalRows * rowHeight) + 50, pageHeight, topMargin);
    
    float tableStartY = currentYRef[0];
    
    // Top border
    contentStreamRef[0].setLineWidth(1.5f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + tableWidth, currentYRef[0]);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    // Helper function to draw a cell
    java.util.function.BiConsumer<String, Float> drawCell = (text, xOffset) -> {
      if (text == null) text = "-";
      String[] lines = wrapText(text, 25);
      float textY = currentYRef[0] - 10;
      for (String line : lines) {
        if (textY < currentYRef[0] - rowHeight + 5) break;
        if (line != null && !line.trim().isEmpty()) {
          try {
            contentStreamRef[0].beginText();
            contentStreamRef[0].setFont(normalFont, 9);
            contentStreamRef[0].newLineAtOffset(margin + xOffset + cellPadding, textY);
            contentStreamRef[0].showText(asciiSafe(line, lang));
            contentStreamRef[0].endText();
          } catch (Exception e) {
            // Ensure endText() is called even on error
            try {
              contentStreamRef[0].endText();
            } catch (Exception ignored) {}
            System.err.println("Error drawing cell: " + e.getMessage());
            e.printStackTrace();
          }
        }
        textY -= lineHeight - 2;
      }
    };
    
    // Helper function to draw label cell (with text wrapping support)
    java.util.function.BiConsumer<String, Float> drawLabelCell = (text, xOffset) -> {
      if (text == null) text = "-";
      try {
        // Calculate available width for label (column width minus padding)
        float availableWidth = col1Width - (cellPadding * 2);
        String[] lines = wrapText(text, (int)(availableWidth / 4)); // Approximate character count
        
        float textY = currentYRef[0] - 10;
        for (String line : lines) {
          if (textY < currentYRef[0] - rowHeight + 5) break;
          if (line != null && !line.trim().isEmpty()) {
            contentStreamRef[0].beginText();
            contentStreamRef[0].setFont(headerFont, 9);
            contentStreamRef[0].newLineAtOffset(margin + xOffset + cellPadding, textY);
            contentStreamRef[0].showText(asciiSafe(line, lang));
            contentStreamRef[0].endText();
          }
          textY -= lineHeight - 2;
        }
      } catch (Exception e) {
        // Ensure endText() is called even on error
        try {
          contentStreamRef[0].endText();
        } catch (Exception ignored) {}
        System.err.println("Error drawing label cell: " + e.getMessage());
        e.printStackTrace();
      }
    };
    
    // Row 1: Grup Şirket Adı | Value | Proje Adı | Value
    float rowBottomY = currentYRef[0] - rowHeight;
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width + col3Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width + col3Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, rowBottomY);
    contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    String groupCompanyLabel = AccidentPdfTranslations.translate(lang, "groupCompanyName");
    if (groupCompanyLabel == null) groupCompanyLabel = "Grup Şirket Adı";
    drawLabelCell.accept(groupCompanyLabel, 0f);
    drawCell.accept(groupCompanyName != null ? groupCompanyName : "-", col1Width);
    String projectLabel = AccidentPdfTranslations.translate(lang, "project");
    if (projectLabel == null) projectLabel = "Proje Adı";
    drawLabelCell.accept(projectLabel, col1Width + col2Width);
    drawCell.accept(projectName != null ? projectName : "-", col1Width + col2Width + col3Width);
    currentYRef[0] = rowBottomY;
    
    // Row 2: Tarih | Value | Saat | Value
    rowBottomY = currentYRef[0] - rowHeight;
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width + col3Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width + col3Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, rowBottomY);
    contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    drawLabelCell.accept(AccidentPdfTranslations.translate(lang, "date"), 0f);
    drawCell.accept(dateStr, col1Width);
    drawLabelCell.accept(AccidentPdfTranslations.translate(lang, "time"), col1Width + col2Width);
    drawCell.accept(timeStr, col1Width + col2Width + col3Width);
    currentYRef[0] = rowBottomY;
    
    // Row 3: Lokasyon | Value | Sorumlu | Value
    rowBottomY = currentYRef[0] - rowHeight;
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width + col3Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width + col3Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, rowBottomY);
    contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    String locationLabel = AccidentPdfTranslations.translate(lang, "location");
    if (locationLabel == null) locationLabel = "Lokasyon";
    drawLabelCell.accept(locationLabel, 0f);
    drawCell.accept(location != null ? location : "-", col1Width);
    String responsibleLabel = AccidentPdfTranslations.translate(lang, "responsiblePerson");
    if (responsibleLabel == null) responsibleLabel = "Sorumlu";
    drawLabelCell.accept(responsibleLabel, col1Width + col2Width);
    drawCell.accept(responsiblePerson != null ? responsiblePerson : "-", col1Width + col2Width + col3Width);
    currentYRef[0] = rowBottomY;
    
    // Row 4: Tahmini Maliyet | Value | İşle İlgili | Value
    rowBottomY = currentYRef[0] - rowHeight;
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width + col3Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width + col3Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, rowBottomY);
    contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    String estimatedCostLabel = AccidentPdfTranslations.translate(lang, "estimatedCost");
    if (estimatedCostLabel == null) estimatedCostLabel = "Tahmini Maliyet";
    drawLabelCell.accept(estimatedCostLabel, 0f);
    drawCell.accept(estimatedCost != null ? estimatedCost : "-", col1Width);
    String workRelatedLabel = "tr".equals(lang) ? "İşle İlgili / İşle İlgisiz" : "en".equals(lang) ? "Work Related / Not Work Related" : "Связано с работой / Не связано с работой";
    drawLabelCell.accept(workRelatedLabel, col1Width + col2Width);
    drawCell.accept(workRelatedDisplay != null ? workRelatedDisplay : "-", col1Width + col2Width + col3Width);
    currentYRef[0] = rowBottomY;
    
    // Row 5: Kaza / Olay Esnasında Yapılan İş | Value (spans 3 columns)
    String workDuringLabel = "tr".equals(lang) ? "Kaza / Olay Esnasında Yapılan İş" : "en".equals(lang) ? "Work During Accident" : "Работа во время происшествия";
    // Calculate how many lines the label will take
    float availableWidth = col1Width - (cellPadding * 2);
    String[] labelLines = wrapText(workDuringLabel, (int)(availableWidth / 4));
    int labelLineCount = Math.max(1, labelLines.length);
    // Adjust row height based on label lines
    float adjustedRowHeight = rowHeight + (labelLineCount - 1) * (lineHeight - 2);
    
    rowBottomY = currentYRef[0] - adjustedRowHeight;
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, rowBottomY);
    contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    drawLabelCell.accept(workDuringLabel, 0f);
    drawCell.accept(workDuringAccident != null ? workDuringAccident : "-", col1Width);
    currentYRef[0] = rowBottomY;
    
    // Row 6: Adı Soyadı | Value | Yaş | Value | Meslek | Value
    rowBottomY = currentYRef[0] - rowHeight;
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    // Draw all vertical lines for 6 columns
    float colWidth6 = tableWidth / 6f;
    for (int i = 1; i < 6; i++) {
      contentStreamRef[0].moveTo(margin + colWidth6 * i, currentYRef[0]);
      contentStreamRef[0].lineTo(margin + colWidth6 * i, rowBottomY);
      contentStreamRef[0].stroke();
    }
    contentStreamRef[0].moveTo(margin, rowBottomY);
    contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    String injuredNameLabel = AccidentPdfTranslations.translate(lang, "injuredPersonName");
    if (injuredNameLabel == null) injuredNameLabel = "Adı Soyadı";
    drawLabelCell.accept(injuredNameLabel, 0f);
    drawCell.accept(injuredPersonName != null ? injuredPersonName : "-", colWidth6);
    String ageLabel = AccidentPdfTranslations.translate(lang, "injuredPersonAge");
    if (ageLabel == null) ageLabel = "Yaş";
    drawLabelCell.accept(ageLabel, colWidth6 * 2);
    drawCell.accept(injuredPersonAge != null ? String.valueOf(injuredPersonAge) : "-", colWidth6 * 3);
    String professionLabel = AccidentPdfTranslations.translate(lang, "injuredPersonProfession");
    if (professionLabel == null) professionLabel = "Meslek";
    drawLabelCell.accept(professionLabel, colWidth6 * 4);
    drawCell.accept(injuredPersonProfession != null ? injuredPersonProfession : "-", colWidth6 * 5);
    currentYRef[0] = rowBottomY;
    
    // Row 7: Cinsiyet | Value | Milliyet | Value
    rowBottomY = currentYRef[0] - rowHeight;
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width + col3Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width + col3Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, rowBottomY);
    contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    String genderLabel = AccidentPdfTranslations.translate(lang, "injuredPersonGender");
    if (genderLabel == null) genderLabel = "Cinsiyet";
    drawLabelCell.accept(genderLabel, 0f);
    drawCell.accept(injuredPersonGender != null ? injuredPersonGender : "-", col1Width);
    String nationalityLabel = AccidentPdfTranslations.translate(lang, "injuredPersonNationality");
    if (nationalityLabel == null) nationalityLabel = "Milliyet";
    drawLabelCell.accept(nationalityLabel, col1Width + col2Width);
    drawCell.accept(injuredPersonNationality != null ? injuredPersonNationality : "-", col1Width + col2Width + col3Width);
    currentYRef[0] = rowBottomY;
    
    // Row 8: Kaza/Olay Sınıflandırması | Value | Çalıştığı Firma | Value
    rowBottomY = currentYRef[0] - rowHeight;
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width + col3Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width + col3Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, rowBottomY);
    contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    String classificationLabel = "tr".equals(lang) ? "Kaza/Olay Sınıflandırması" : "en".equals(lang) ? "Accident/Event Classification" : "Классификация происшествия";
    drawLabelCell.accept(classificationLabel, 0f);
    drawCell.accept(accidentClassification != null ? accidentClassification : "-", col1Width);
    String companyLabel = AccidentPdfTranslations.translate(lang, "injuredPersonCompany");
    if (companyLabel == null) companyLabel = "Çalıştığı Firma";
    drawLabelCell.accept(companyLabel, col1Width + col2Width);
    drawCell.accept(injuredPersonCompany != null ? injuredPersonCompany : "-", col1Width + col2Width + col3Width);
    currentYRef[0] = rowBottomY;
    
    // Row 9: Kaza/Olay Türü | Value
    rowBottomY = currentYRef[0] - rowHeight;
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, rowBottomY);
    contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    String accidentTypeLabel = "tr".equals(lang) ? "Kaza/Olay Türü" : "en".equals(lang) ? "Accident/Event Type" : "Тип происшествия";
    drawLabelCell.accept(accidentTypeLabel, 0f);
    drawCell.accept(accidentTypeCode != null ? accidentTypeCode : "-", col1Width);
    currentYRef[0] = rowBottomY;
    
    // Row 10: Yaralanma Türü | Value | Yaralanan Vücut Bölgesi | Value
    rowBottomY = currentYRef[0] - rowHeight;
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width + col3Width, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width + col3Width, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, rowBottomY);
    contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    String injuryTypeLabel = AccidentPdfTranslations.translate(lang, "injuryType");
    if (injuryTypeLabel == null) injuryTypeLabel = "Yaralanma Türü";
    drawLabelCell.accept(injuryTypeLabel, 0f);
    drawCell.accept(injuryTypeDisplay != null ? injuryTypeDisplay : "-", col1Width);
    String bodyPartLabel = AccidentPdfTranslations.translate(lang, "injuredBodyPart");
    if (bodyPartLabel == null) bodyPartLabel = "Yaralanan Vücut Bölgesi";
    drawLabelCell.accept(bodyPartLabel, col1Width + col2Width);
    drawCell.accept(bodyPartDisplay != null ? bodyPartDisplay : "-", col1Width + col2Width + col3Width);
    currentYRef[0] = rowBottomY;
    
    // Draw final borders
    contentStreamRef[0].setLineWidth(1.5f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentYRef[0]);
    contentStreamRef[0].lineTo(margin + tableWidth, currentYRef[0]);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, tableStartY);
    contentStreamRef[0].lineTo(margin, currentYRef[0]);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + tableWidth, tableStartY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentYRef[0]);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    return currentYRef[0];
  }
  
  // Draw Direct Causes section (Direkt Nedenler) as a table
  private float drawDirectCausesSection(PDPageContentStream[] contentStreamRef, PDDocument document,
      PDPage[] currentPageRef, float margin, float y, JsonNode rootCauseData, String lang,
      float pageHeight, float bottomMargin, float topMargin, org.apache.pdfbox.pdmodel.font.PDFont headerFont,
      org.apache.pdfbox.pdmodel.font.PDFont normalFont, float lineHeight) throws IOException {
    
    float currentY = y;
    float pageWidth = pageHeight * 0.707f; // A4 ratio
    float tableWidth = pageWidth - 2 * margin;
    float col1Width = tableWidth * 0.4f; // Category column
    float col2Width = tableWidth * 0.6f; // Items column
    float cellPadding = 8f;
    float rowHeight = 18f;
    
    // Collect items for both categories
    List<String> unsafeBehaviorItems = new ArrayList<>();
    JsonNode unsafeBehavior = rootCauseData.get("unsafeBehavior");
    if (unsafeBehavior != null && unsafeBehavior.isArray()) {
      for (JsonNode item : unsafeBehavior) {
        if (item.isTextual()) {
          unsafeBehaviorItems.add(translateRootCauseOption(lang, item.asText()));
        }
      }
    }
    
    List<String> unsafeConditionItems = new ArrayList<>();
    JsonNode unsafeCondition = rootCauseData.get("unsafeCondition");
    if (unsafeCondition != null && unsafeCondition.isArray()) {
      for (JsonNode item : unsafeCondition) {
        if (item.isTextual()) {
          unsafeConditionItems.add(translateRootCauseOption(lang, item.asText()));
        }
      }
    }
    
    // Calculate total rows needed
    int totalRows = Math.max(unsafeBehaviorItems.size(), unsafeConditionItems.size()) + 1; // +1 for header
    if (unsafeBehaviorItems.isEmpty() && unsafeConditionItems.isEmpty()) {
      return currentY; // Nothing to draw
    }
    
    // Draw table header
    currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + (totalRows * rowHeight) + 50, pageHeight, topMargin);
    
    // Top border
    contentStreamRef[0].setLineWidth(1.5f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    float tableStartY = currentY;
    
    // Header row
    String headerLabel = "tr".equals(lang) ? "Direkt Nedenler" : "en".equals(lang) ? "Direct Causes" : "Прямые причины";
    contentStreamRef[0].setLineWidth(1f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentY);
    contentStreamRef[0].lineTo(margin + col1Width, currentY - rowHeight);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    contentStreamRef[0].beginText();
    contentStreamRef[0].setFont(headerFont, 10);
    contentStreamRef[0].newLineAtOffset(margin + cellPadding, currentY - 12);
    contentStreamRef[0].showText(asciiSafe(headerLabel, lang));
    contentStreamRef[0].endText();
    
    currentY -= rowHeight;
    
    // Draw rows
    int maxRows = Math.max(unsafeBehaviorItems.size(), unsafeConditionItems.size());
    for (int i = 0; i < maxRows; i++) {
      if (currentY - rowHeight < bottomMargin + 30) {
        // Close current page borders
        contentStreamRef[0].setLineWidth(1f);
        contentStreamRef[0].moveTo(margin, currentY);
        contentStreamRef[0].lineTo(margin + tableWidth, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].moveTo(margin, tableStartY);
        contentStreamRef[0].lineTo(margin, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].moveTo(margin + col1Width, tableStartY);
        contentStreamRef[0].lineTo(margin + col1Width, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].moveTo(margin + tableWidth, tableStartY);
        contentStreamRef[0].lineTo(margin + tableWidth, currentY);
        contentStreamRef[0].stroke();
        
        // New page
        contentStreamRef[0].close();
        PDPage newPage = new PDPage(PDRectangle.A4);
        document.addPage(newPage);
        currentPageRef[0] = newPage;
        contentStreamRef[0] = new PDPageContentStream(document, newPage);
        currentY = pageHeight - topMargin;
        tableStartY = currentY;
        
        // Redraw top border
        contentStreamRef[0].setLineWidth(1.5f);
        contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
        contentStreamRef[0].moveTo(margin, currentY);
        contentStreamRef[0].lineTo(margin + tableWidth, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].setStrokingColor(0, 0, 0);
      }
      
      float rowBottomY = currentY - rowHeight;
      
      // Draw vertical line
      contentStreamRef[0].setLineWidth(0.5f);
      contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
      contentStreamRef[0].moveTo(margin + col1Width, currentY);
      contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
      contentStreamRef[0].stroke();
      
      // Draw horizontal line
      contentStreamRef[0].moveTo(margin, rowBottomY);
      contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
      contentStreamRef[0].stroke();
      contentStreamRef[0].setStrokingColor(0, 0, 0);
      
      // Category name (only for first row of each category)
      if (i == 0) {
        String unsafeBehaviorLabel = "tr".equals(lang) ? "Güvensiz Davranış" : "en".equals(lang) ? "Unsafe Behavior" : "Небезопасное поведение";
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(headerFont, 9);
        contentStreamRef[0].newLineAtOffset(margin + cellPadding, currentY - 12);
        contentStreamRef[0].showText(asciiSafe(unsafeBehaviorLabel, lang));
        contentStreamRef[0].endText();
      } else if (i == unsafeBehaviorItems.size() && unsafeConditionItems.size() > 0) {
        String unsafeConditionLabel = "tr".equals(lang) ? "Güvensiz Durum" : "en".equals(lang) ? "Unsafe Condition" : "Небезопасное состояние";
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(headerFont, 9);
        contentStreamRef[0].newLineAtOffset(margin + cellPadding, currentY - 12);
        contentStreamRef[0].showText(asciiSafe(unsafeConditionLabel, lang));
        contentStreamRef[0].endText();
      }
      
      // Items column
      String itemText = "";
      if (i < unsafeBehaviorItems.size()) {
        itemText = "• " + unsafeBehaviorItems.get(i);
      } else if (i >= unsafeBehaviorItems.size() && (i - unsafeBehaviorItems.size()) < unsafeConditionItems.size()) {
        itemText = "• " + unsafeConditionItems.get(i - unsafeBehaviorItems.size());
      }
      
      if (!itemText.isEmpty()) {
        String[] itemLines = wrapText(itemText, 60);
        float textY = currentY - 8;
        for (String line : itemLines) {
          if (textY < rowBottomY + 5) break;
          contentStreamRef[0].beginText();
          contentStreamRef[0].setFont(normalFont, 9);
          contentStreamRef[0].newLineAtOffset(margin + col1Width + cellPadding, textY);
          contentStreamRef[0].showText(asciiSafe(line, lang));
          contentStreamRef[0].endText();
          textY -= lineHeight - 2;
        }
      }
      
      currentY = rowBottomY;
    }
    
    // Draw final borders
    contentStreamRef[0].setLineWidth(1.5f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, tableStartY);
    contentStreamRef[0].lineTo(margin, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + tableWidth, tableStartY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    return currentY;
  }
  
  // Draw Root Causes section (Kök Nedenler) as a table
  private float drawRootCausesSection(PDPageContentStream[] contentStreamRef, PDDocument document,
      PDPage[] currentPageRef, float margin, float y, JsonNode rootCauseData, String lang,
      float pageHeight, float bottomMargin, float topMargin, org.apache.pdfbox.pdmodel.font.PDFont headerFont,
      org.apache.pdfbox.pdmodel.font.PDFont normalFont, float lineHeight) throws IOException {
    
    float currentY = y;
    float pageWidth = pageHeight * 0.707f; // A4 ratio
    float tableWidth = pageWidth - 2 * margin;
    float col1Width = tableWidth * 0.4f; // Category column
    float col2Width = tableWidth * 0.6f; // Items column
    float cellPadding = 8f;
    float rowHeight = 18f;
    
    // Collect items for both categories
    List<String> personalFactorsItems = new ArrayList<>();
    JsonNode personalFactors = rootCauseData.get("personalFactors");
    if (personalFactors != null && personalFactors.isArray()) {
      for (JsonNode item : personalFactors) {
        if (item.isTextual()) {
          personalFactorsItems.add(translateRootCauseOption(lang, item.asText()));
        }
      }
    }
    
    List<String> workFactorsItems = new ArrayList<>();
    JsonNode workFactors = rootCauseData.get("workFactors");
    if (workFactors != null && workFactors.isArray()) {
      for (JsonNode item : workFactors) {
        if (item.isTextual()) {
          workFactorsItems.add(translateRootCauseOption(lang, item.asText()));
        }
      }
    }
    
    // Calculate total rows needed
    int totalRows = Math.max(personalFactorsItems.size(), workFactorsItems.size()) + 1; // +1 for header
    if (personalFactorsItems.isEmpty() && workFactorsItems.isEmpty()) {
      return currentY; // Nothing to draw
    }
    
    // Draw table header
    currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + (totalRows * rowHeight) + 50, pageHeight, topMargin);
    
    // Top border
    contentStreamRef[0].setLineWidth(1.5f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    float tableStartY = currentY;
    
    // Header row
    String headerLabel = "tr".equals(lang) ? "Kök Nedenler" : "en".equals(lang) ? "Root Causes" : "Корневые причины";
    contentStreamRef[0].setLineWidth(1f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentY);
    contentStreamRef[0].lineTo(margin + col1Width, currentY - rowHeight);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    contentStreamRef[0].beginText();
    contentStreamRef[0].setFont(headerFont, 10);
    contentStreamRef[0].newLineAtOffset(margin + cellPadding, currentY - 12);
    contentStreamRef[0].showText(asciiSafe(headerLabel, lang));
    contentStreamRef[0].endText();
    
    currentY -= rowHeight;
    
    // Draw rows
    int maxRows = Math.max(personalFactorsItems.size(), workFactorsItems.size());
    for (int i = 0; i < maxRows; i++) {
      if (currentY - rowHeight < bottomMargin + 30) {
        // Close current page borders
        contentStreamRef[0].setLineWidth(1f);
        contentStreamRef[0].moveTo(margin, currentY);
        contentStreamRef[0].lineTo(margin + tableWidth, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].moveTo(margin, tableStartY);
        contentStreamRef[0].lineTo(margin, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].moveTo(margin + col1Width, tableStartY);
        contentStreamRef[0].lineTo(margin + col1Width, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].moveTo(margin + tableWidth, tableStartY);
        contentStreamRef[0].lineTo(margin + tableWidth, currentY);
        contentStreamRef[0].stroke();
        
        // New page
        contentStreamRef[0].close();
        PDPage newPage = new PDPage(PDRectangle.A4);
        document.addPage(newPage);
        currentPageRef[0] = newPage;
        contentStreamRef[0] = new PDPageContentStream(document, newPage);
        currentY = pageHeight - topMargin;
        tableStartY = currentY;
        
        // Redraw top border
        contentStreamRef[0].setLineWidth(1.5f);
        contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
        contentStreamRef[0].moveTo(margin, currentY);
        contentStreamRef[0].lineTo(margin + tableWidth, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].setStrokingColor(0, 0, 0);
      }
      
      float rowBottomY = currentY - rowHeight;
      
      // Draw vertical line
      contentStreamRef[0].setLineWidth(0.5f);
      contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
      contentStreamRef[0].moveTo(margin + col1Width, currentY);
      contentStreamRef[0].lineTo(margin + col1Width, rowBottomY);
      contentStreamRef[0].stroke();
      
      // Draw horizontal line
      contentStreamRef[0].moveTo(margin, rowBottomY);
      contentStreamRef[0].lineTo(margin + tableWidth, rowBottomY);
      contentStreamRef[0].stroke();
      contentStreamRef[0].setStrokingColor(0, 0, 0);
      
      // Category name (only for first row of each category)
      if (i == 0) {
        String personalFactorsLabel = "tr".equals(lang) ? "Kişisel Faktörler" : "en".equals(lang) ? "Personal Factors" : "Личные факторы";
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(headerFont, 9);
        contentStreamRef[0].newLineAtOffset(margin + cellPadding, currentY - 12);
        contentStreamRef[0].showText(asciiSafe(personalFactorsLabel, lang));
        contentStreamRef[0].endText();
      } else if (i == personalFactorsItems.size() && workFactorsItems.size() > 0) {
        String workFactorsLabel = "tr".equals(lang) ? "İş Faktörleri" : "en".equals(lang) ? "Work Factors" : "Рабочие факторы";
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(headerFont, 9);
        contentStreamRef[0].newLineAtOffset(margin + cellPadding, currentY - 12);
        contentStreamRef[0].showText(asciiSafe(workFactorsLabel, lang));
        contentStreamRef[0].endText();
      }
      
      // Items column
      String itemText = "";
      if (i < personalFactorsItems.size()) {
        itemText = "• " + personalFactorsItems.get(i);
      } else if (i >= personalFactorsItems.size() && (i - personalFactorsItems.size()) < workFactorsItems.size()) {
        itemText = "• " + workFactorsItems.get(i - personalFactorsItems.size());
      }
      
      if (!itemText.isEmpty()) {
        String[] itemLines = wrapText(itemText, 60);
        float textY = currentY - 8;
        for (String line : itemLines) {
          if (textY < rowBottomY + 5) break;
          contentStreamRef[0].beginText();
          contentStreamRef[0].setFont(normalFont, 9);
          contentStreamRef[0].newLineAtOffset(margin + col1Width + cellPadding, textY);
          contentStreamRef[0].showText(asciiSafe(line, lang));
          contentStreamRef[0].endText();
          textY -= lineHeight - 2;
        }
      }
      
      currentY = rowBottomY;
    }
    
    // Draw final borders
    contentStreamRef[0].setLineWidth(1.5f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin, tableStartY);
    contentStreamRef[0].lineTo(margin, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + tableWidth, tableStartY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    return currentY;
  }
  
  // Draw Actions Table
  private float drawActionsTable(PDPageContentStream[] contentStreamRef, PDDocument document,
      PDPage[] currentPageRef, float margin, float y, JsonNode actionsArray, String lang,
      float pageHeight, float bottomMargin, float topMargin, org.apache.pdfbox.pdmodel.font.PDFont headerFont,
      org.apache.pdfbox.pdmodel.font.PDFont normalFont, float lineHeight, float tableWidth) throws IOException {
    
    float currentY = y;
    float cellPadding = 5f;
    float rowHeight = 20f;
    float headerHeight = 25f;
    
    // Table columns: Action, Responsible Person, Target Date
    float col1Width = tableWidth * 0.5f; // Action
    float col2Width = tableWidth * 0.25f; // Responsible Person
    float col3Width = tableWidth * 0.25f; // Target Date
    
    // Draw table header
    contentStreamRef[0].setLineWidth(1.5f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    String actionLabel = "tr".equals(lang) ? "Aksiyon" : "en".equals(lang) ? "Action" : "Действие";
    String responsibleLabel = "tr".equals(lang) ? "Sorumlu Kişi" : "en".equals(lang) ? "Responsible Person" : "Ответственное лицо";
    String targetDateLabel = "tr".equals(lang) ? "Hedeflenen Kapanma Tarihi" : "en".equals(lang) ? "Target Closure Date" : "Целевая дата закрытия";
    
    contentStreamRef[0].beginText();
    contentStreamRef[0].setFont(headerFont, 9);
    contentStreamRef[0].newLineAtOffset(margin + cellPadding, currentY - 12);
    contentStreamRef[0].showText(asciiSafe(actionLabel, lang));
    contentStreamRef[0].endText();
    
    contentStreamRef[0].beginText();
    contentStreamRef[0].setFont(headerFont, 9);
    contentStreamRef[0].newLineAtOffset(margin + col1Width + cellPadding, currentY - 12);
    contentStreamRef[0].showText(asciiSafe(responsibleLabel, lang));
    contentStreamRef[0].endText();
    
    contentStreamRef[0].beginText();
    contentStreamRef[0].setFont(headerFont, 9);
    contentStreamRef[0].newLineAtOffset(margin + col1Width + col2Width + cellPadding, currentY - 12);
    contentStreamRef[0].showText(asciiSafe(targetDateLabel, lang));
    contentStreamRef[0].endText();
    
    currentY -= headerHeight;
    
    // Draw vertical lines
    contentStreamRef[0].setLineWidth(0.5f);
    contentStreamRef[0].setStrokingColor(0.7f, 0.7f, 0.7f);
    contentStreamRef[0].moveTo(margin + col1Width, currentY + headerHeight);
    contentStreamRef[0].lineTo(margin + col1Width, currentY - (actionsArray.size() * rowHeight));
    contentStreamRef[0].stroke();
    contentStreamRef[0].moveTo(margin + col1Width + col2Width, currentY + headerHeight);
    contentStreamRef[0].lineTo(margin + col1Width + col2Width, currentY - (actionsArray.size() * rowHeight));
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    // Draw rows
    for (JsonNode actionNode : actionsArray) {
      if (currentY - rowHeight < bottomMargin + 30) {
        currentY = checkAndNewPage(contentStreamRef, document, currentPageRef, currentY, bottomMargin + 50, pageHeight, topMargin);
        // Redraw header on new page
        contentStreamRef[0].setLineWidth(1.5f);
        contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
        contentStreamRef[0].moveTo(margin, currentY);
        contentStreamRef[0].lineTo(margin + tableWidth, currentY);
        contentStreamRef[0].stroke();
        contentStreamRef[0].setStrokingColor(0, 0, 0);
        
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(headerFont, 9);
        contentStreamRef[0].newLineAtOffset(margin + cellPadding, currentY - 12);
        contentStreamRef[0].showText(asciiSafe(actionLabel, lang));
        contentStreamRef[0].endText();
        
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(headerFont, 9);
        contentStreamRef[0].newLineAtOffset(margin + col1Width + cellPadding, currentY - 12);
        contentStreamRef[0].showText(asciiSafe(responsibleLabel, lang));
        contentStreamRef[0].endText();
        
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(headerFont, 9);
        contentStreamRef[0].newLineAtOffset(margin + col1Width + col2Width + cellPadding, currentY - 12);
        contentStreamRef[0].showText(asciiSafe(targetDateLabel, lang));
        contentStreamRef[0].endText();
        
        currentY -= headerHeight;
      }
      
      String action = actionNode.has("action") ? actionNode.get("action").asText() : "-";
      String responsible = actionNode.has("responsiblePerson") ? actionNode.get("responsiblePerson").asText() : "-";
      String targetDate = "-";
      if (actionNode.has("targetDate") && !actionNode.get("targetDate").isNull()) {
        try {
          String dateStr = actionNode.get("targetDate").asText();
          if (dateStr != null && !dateStr.isEmpty()) {
            // Parse ISO date and format as dd.MM.yyyy
            java.time.Instant instant = java.time.Instant.parse(dateStr);
            targetDate = instant.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
          }
        } catch (Exception e) {
          // Keep as "-"
        }
      }
      
      // Draw row border
      contentStreamRef[0].setLineWidth(0.5f);
      contentStreamRef[0].moveTo(margin, currentY);
      contentStreamRef[0].lineTo(margin + tableWidth, currentY);
      contentStreamRef[0].stroke();
      
      // Draw action text (wrap if needed)
      String[] actionLines = wrapText(action, 50);
      float textY = currentY - 8;
      for (String line : actionLines) {
        if (textY < currentY - rowHeight + 5) break;
        contentStreamRef[0].beginText();
        contentStreamRef[0].setFont(normalFont, 8);
        contentStreamRef[0].newLineAtOffset(margin + cellPadding, textY);
        contentStreamRef[0].showText(asciiSafe(line, lang));
        contentStreamRef[0].endText();
        textY -= lineHeight - 2;
      }
      
      // Draw responsible person
      contentStreamRef[0].beginText();
      contentStreamRef[0].setFont(normalFont, 8);
      contentStreamRef[0].newLineAtOffset(margin + col1Width + cellPadding, currentY - 8);
      contentStreamRef[0].showText(asciiSafe(responsible, lang));
      contentStreamRef[0].endText();
      
      // Draw target date
      contentStreamRef[0].beginText();
      contentStreamRef[0].setFont(normalFont, 8);
      contentStreamRef[0].newLineAtOffset(margin + col1Width + col2Width + cellPadding, currentY - 8);
      contentStreamRef[0].showText(targetDate);
      contentStreamRef[0].endText();
      
      currentY -= rowHeight;
    }
    
    // Draw bottom border
    contentStreamRef[0].setLineWidth(1.5f);
    contentStreamRef[0].setStrokingColor(0.176f, 0.749f, 0.506f);
    contentStreamRef[0].moveTo(margin, currentY);
    contentStreamRef[0].lineTo(margin + tableWidth, currentY);
    contentStreamRef[0].stroke();
    contentStreamRef[0].setStrokingColor(0, 0, 0);
    
    return currentY;
  }
  
  // Translate root cause option keys to display text
  private String translateRootCauseOption(String lang, String key) {
    Map<String, Map<String, String>> translations = new HashMap<>();
    
    Map<String, String> trMap = new HashMap<>();
    trMap.put("INDIVIDUAL_VIOLATION", "1.1 Bireysel İhlal");
    trMap.put("ENVIRONMENT_NEGLECT", "4.3 Çevre ve Çalışma Alanına Dikkat Etmeme");
    trMap.put("WRONG_POSTURE", "1.5 Hatalı Dururş / Pozisyon");
    trMap.put("LACK_OF_ATTENTION", "4 Dikkat / Farkındalık Eksikliği");
    trMap.put("THOUGHTLESS_ROUTINE", "4.8 Düşünmeden Yapılan Rutin Aktivite");
    trMap.put("PROTECTION_SYSTEMS", "5 Koruma Sistemleri");
    trMap.put("WORKPLACE_ARRANGEMENT", "8 Çalışmma Yeri / Düzeni");
    trMap.put("TRAINING_KNOWLEDGE", "7 Eğitim / Bilgi");
    trMap.put("EMPLOYEE_RUSH", "5.4 Çalışanın Acele Etmesi Gerektiğini Düşünmesi");
    trMap.put("LACK_OF_CONCENTRATION", "4.7 Konsantrasyon Eksikliği");
    trMap.put("INSUFFICIENT_CONTROL", "12.8 Yetersiz Kontrol, Denetim ve İzleme");
    
    Map<String, String> enMap = new HashMap<>();
    enMap.put("INDIVIDUAL_VIOLATION", "1.1 Individual Violation");
    enMap.put("ENVIRONMENT_NEGLECT", "4.3 Neglecting Environment and Work Area");
    enMap.put("WRONG_POSTURE", "1.5 Wrong Posture / Position");
    enMap.put("LACK_OF_ATTENTION", "4 Lack of Attention / Awareness");
    enMap.put("THOUGHTLESS_ROUTINE", "4.8 Thoughtless Routine Activity");
    enMap.put("PROTECTION_SYSTEMS", "5 Protection Systems");
    enMap.put("WORKPLACE_ARRANGEMENT", "8 Workplace / Arrangement");
    enMap.put("TRAINING_KNOWLEDGE", "7 Training / Knowledge");
    enMap.put("EMPLOYEE_RUSH", "5.4 Employee Feeling Rushed");
    enMap.put("LACK_OF_CONCENTRATION", "4.7 Lack of Concentration");
    enMap.put("INSUFFICIENT_CONTROL", "12.8 Insufficient Control, Supervision and Monitoring");
    
    Map<String, String> ruMap = new HashMap<>();
    ruMap.put("INDIVIDUAL_VIOLATION", "1.1 Индивидуальное нарушение");
    ruMap.put("ENVIRONMENT_NEGLECT", "4.3 Пренебрежение окружающей средой и рабочим местом");
    ruMap.put("WRONG_POSTURE", "1.5 Неправильная осанка / Позиция");
    ruMap.put("LACK_OF_ATTENTION", "4 Недостаток внимания / Осведомленности");
    ruMap.put("THOUGHTLESS_ROUTINE", "4.8 Бездумная рутинная деятельность");
    ruMap.put("PROTECTION_SYSTEMS", "5 Системы защиты");
    ruMap.put("WORKPLACE_ARRANGEMENT", "8 Рабочее место / Организация");
    ruMap.put("TRAINING_KNOWLEDGE", "7 Обучение / Знания");
    ruMap.put("EMPLOYEE_RUSH", "5.4 Ощущение работником спешки");
    ruMap.put("LACK_OF_CONCENTRATION", "4.7 Недостаток концентрации");
    ruMap.put("INSUFFICIENT_CONTROL", "12.8 Недостаточный контроль, надзор и мониторинг");
    
    translations.put("tr", trMap);
    translations.put("en", enMap);
    translations.put("ru", ruMap);
    
    return translations.getOrDefault(lang, trMap).getOrDefault(key, key);
  }
}
