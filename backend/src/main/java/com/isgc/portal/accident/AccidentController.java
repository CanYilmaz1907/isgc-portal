package com.isgc.portal.accident;

import com.isgc.portal.accident.dto.AccidentResponse;
import com.isgc.portal.accident.dto.AccidentTypeRequest;
import com.isgc.portal.accident.dto.AccidentTypeResponse;
import com.isgc.portal.accident.dto.AccidentUpsertRequest;
import com.isgc.portal.accident.dto.NotificationRuleRequest;
import com.isgc.portal.accident.dto.NotificationRuleResponse;
import com.isgc.portal.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/accidents")
public class AccidentController {
  private static final Logger log = LoggerFactory.getLogger(AccidentController.class);
  private final AccidentService accidentService;
  private final AccidentTypeRepository accidentTypeRepository;
  private final NotificationRuleRepository notificationRuleRepository;
  private final AccidentPdfService pdfService;
  private final AccidentPersonRepository accidentPersonRepository;
  private final AccidentListExportService listExportService;

  public AccidentController(
      AccidentService accidentService,
      AccidentTypeRepository accidentTypeRepository,
      NotificationRuleRepository notificationRuleRepository,
      AccidentPdfService pdfService,
      AccidentPersonRepository accidentPersonRepository,
      AccidentListExportService listExportService
  ) {
    this.accidentService = accidentService;
    this.accidentTypeRepository = accidentTypeRepository;
    this.notificationRuleRepository = notificationRuleRepository;
    this.pdfService = pdfService;
    this.accidentPersonRepository = accidentPersonRepository;
    this.listExportService = listExportService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<AccidentResponse> list(@AuthenticationPrincipal @NotNull CurrentUser user) {
    return accidentService.list(user);
  }

  @GetMapping("/export")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public ResponseEntity<byte[]> exportList(@AuthenticationPrincipal @NotNull CurrentUser user) throws Exception {
    byte[] data = listExportService.exportExcel(user);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"kaza-listesi.xlsx\"")
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(data);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public AccidentResponse get(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return accidentService.get(user, id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public AccidentResponse create(@Valid @RequestBody AccidentUpsertRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return accidentService.create(user, req);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public AccidentResponse update(@PathVariable("id") UUID id, @Valid @RequestBody AccidentUpsertRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return accidentService.update(user, id, req);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    accidentService.delete(user, id);
  }

  @GetMapping("/types")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<AccidentTypeResponse> listTypes() {
    return accidentTypeRepository.findByEnabledTrue().stream()
        .map(t -> new AccidentTypeResponse(t.getId(), t.getCode(), t.getName(), t.getFormSchema(), t.isEnabled()))
        .toList();
  }

  @PostMapping("/types")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public AccidentTypeResponse createType(@RequestBody @Valid AccidentTypeRequest req) {
    AccidentType t = new AccidentType();
    t.setId(UUID.randomUUID());
    t.setCode(req.code());
    t.setName(req.name());
    t.setFormSchema(req.formSchemaJson());
    t.setEnabled(req.enabled());
    accidentTypeRepository.save(t);
    return new AccidentTypeResponse(t.getId(), t.getCode(), t.getName(), t.getFormSchema(), t.isEnabled());
  }

  @PostMapping("/notification-rules")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public NotificationRuleResponse createRule(@RequestBody @Valid NotificationRuleRequest req) {
    NotificationRule r = new NotificationRule();
    r.setId(UUID.randomUUID());
    r.setAccidentClass(req.accidentClass());
    r.setPotentialLevel(req.potentialLevel());
    r.setToEmails(req.toEmails());
    r.setCcEmails(req.ccEmails());
    r.setEnabled(req.enabled());
    notificationRuleRepository.save(r);
    return new NotificationRuleResponse(r.getId(), r.getAccidentClass(), r.getPotentialLevel(), r.getToEmails(), r.getCcEmails(), r.isEnabled());
  }

  @GetMapping("/notification-rules")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public List<NotificationRuleResponse> listRules() {
    return notificationRuleRepository.findAll().stream()
        .map(r -> new NotificationRuleResponse(r.getId(), r.getAccidentClass(), r.getPotentialLevel(), r.getToEmails(), r.getCcEmails(), r.isEnabled()))
        .toList();
  }

  @GetMapping("/{id}/pdf")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public ResponseEntity<byte[]> generatePdf(
      @PathVariable("id") UUID id, 
      @RequestParam(value = "lang", defaultValue = "tr") String lang,
      @AuthenticationPrincipal @NotNull CurrentUser user
  ) {
    try {
      log.info("Generating PDF for accident: {} with language: {}", id, lang);
      
      // Access check + lazy-load relations needed for PDF
      accidentService.getEntityWithRelations(user, id);
      AccidentResponse response = accidentService.get(user, id);
      
      // Fetch AccidentPerson entities with Employee relationships eagerly loaded
      List<AccidentPerson> allPeople = accidentPersonRepository.findByAccidentIdWithEmployee(id);
      List<AccidentPerson> injured = allPeople.stream()
          .filter(p -> p.getRole() == AccidentPersonRole.INJURED)
          .toList();
      List<AccidentPerson> keyPeople = allPeople.stream()
          .filter(p -> p.getRole() == AccidentPersonRole.KEY_PERSON)
          .toList();

      log.info("Calling PDF service for accident: {}", id);
      byte[] pdfBytes = pdfService.generatePdf(response, injured, keyPeople, lang);
      log.info("PDF generated successfully, size: {} bytes", pdfBytes.length);
      
      String filenamePrefix = "tr".equals(lang) ? "kaza-raporu" : 
                               "en".equals(lang) ? "accident-report" : 
                               "ru".equals(lang) ? "otchet-o-neschastnom-sluchae" : "kaza-raporu";
      String filename = filenamePrefix + "-" + id.toString().substring(0, 8) + ".pdf";

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .contentType(MediaType.APPLICATION_PDF)
          .contentLength(pdfBytes.length)
          .body(pdfBytes);
    } catch (IllegalArgumentException e) {
      log.error("Illegal argument exception while generating PDF for accident: {}", id, e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error while generating PDF for accident: {}", id, e);
      throw new IllegalStateException("PDF generation failed: " + e.getMessage(), e);
    }
  }
}


