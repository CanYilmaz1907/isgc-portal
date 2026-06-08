package com.isgc.portal.discipline;

import com.isgc.portal.discipline.dto.DisciplineMetadataResponse;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discipline-logs/metadata")
public class DisciplineMetadataController {
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public DisciplineMetadataResponse metadata() {
    Map<String, List<DisciplineMetadataResponse.Option>> violationTypes = new LinkedHashMap<>();
    for (var entry : DisciplineViolationTypes.all().entrySet()) {
      violationTypes.put(
          entry.getKey().name(),
          entry.getValue().stream()
              .map(v -> new DisciplineMetadataResponse.Option(v.code(), v.labelTr()))
              .toList()
      );
    }

    List<DisciplineMetadataResponse.Option> categories = Arrays.stream(DisciplineCategory.values())
        .map(c -> new DisciplineMetadataResponse.Option(c.name(), categoryLabel(c)))
        .toList();

    List<DisciplineMetadataResponse.Option> statuses = Arrays.stream(DisciplineStatus.values())
        .map(s -> new DisciplineMetadataResponse.Option(s.name(), statusLabel(s)))
        .toList();

    return new DisciplineMetadataResponse(categories, violationTypes, statuses);
  }

  private static String categoryLabel(DisciplineCategory c) {
    return switch (c) {
      case CAT_0 -> "Kategori 0 — Direkt Çıkış";
      case CAT_1 -> "Kategori 1 — 6 ay içinde 2 ihtar";
      case CAT_2 -> "Kategori 2 — 6 ay içinde 3 ihtar";
      case CAT_3 -> "Kategori 3 — 6 ay içinde 4 ihtar";
    };
  }

  private static String statusLabel(DisciplineStatus s) {
    return switch (s) {
      case SOZLU_UYARI -> "Sözlü Uyarı";
      case UYARI -> "Uyarı";
      case IDARI_CEZA -> "İdari Ceza";
      case SOZLESME_FESHI -> "Sözleşme Feshi";
    };
  }
}
