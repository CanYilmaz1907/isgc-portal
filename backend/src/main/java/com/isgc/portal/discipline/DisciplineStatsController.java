package com.isgc.portal.discipline;

import com.isgc.portal.discipline.dto.DisciplineSummaryStatsResponse;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discipline-logs/stats")
public class DisciplineStatsController {
  private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

  private final DisciplineLogRepository repo;

  public DisciplineStatsController(DisciplineLogRepository repo) {
    this.repo = repo;
  }

  @GetMapping("/by-category")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public Map<String, Long> byCategory() {
    Map<String, Long> out = new HashMap<>();
    for (DisciplineLog d : repo.findAll()) {
      String key = d.getCategoryLevel() != null ? d.getCategoryLevel().name() : "Tanımsız";
      out.put(key, out.getOrDefault(key, 0L) + 1L);
    }
    return out;
  }

  @GetMapping("/by-severity")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public Map<String, Long> byViolationType() {
    Map<String, Long> out = new HashMap<>();
    for (DisciplineLog d : repo.findAll()) {
      String key = d.getViolationType() != null
          ? DisciplineViolationTypes.labelFor(d.getViolationType())
          : "Tanımsız";
      out.put(key, out.getOrDefault(key, 0L) + 1L);
    }
    return out;
  }

  @GetMapping("/summary")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public DisciplineSummaryStatsResponse summary() {
    long totalWarnings = 0;
    long totalPenalties = 0;
    Map<String, Long> byCategory = new HashMap<>();
    Map<String, Long> byMonth = new HashMap<>();
    Map<String, Long> byResponsible = new HashMap<>();
    Map<String, Long> byCompany = new HashMap<>();

    for (DisciplineLog d : repo.findAll()) {
      if (d.getStatus() == DisciplineStatus.UYARI || d.getStatus() == DisciplineStatus.SOZLU_UYARI) {
        totalWarnings++;
      }
      if (d.getStatus() == DisciplineStatus.IDARI_CEZA || d.getStatus() == DisciplineStatus.SOZLESME_FESHI) {
        totalPenalties++;
      }

      String cat = d.getCategoryLevel() != null ? d.getCategoryLevel().name() : "Tanımsız";
      byCategory.put(cat, byCategory.getOrDefault(cat, 0L) + 1L);

      if (d.getOccurredAt() != null) {
        String month = d.getOccurredAt().atZone(ZoneId.systemDefault()).format(MONTH_FMT);
        byMonth.put(month, byMonth.getOrDefault(month, 0L) + 1L);
      }

      String resp = d.getResponsiblePerson() != null && !d.getResponsiblePerson().isBlank()
          ? d.getResponsiblePerson() : "Tanımsız";
      byResponsible.put(resp, byResponsible.getOrDefault(resp, 0L) + 1L);

      String company = d.getCompany() != null && !d.getCompany().isBlank() ? d.getCompany() : "Tanımsız";
      byCompany.put(company, byCompany.getOrDefault(company, 0L) + 1L);
    }

    return new DisciplineSummaryStatsResponse(
        totalWarnings,
        totalPenalties,
        byCategory,
        byMonth,
        byResponsible,
        byCompany
    );
  }
}
