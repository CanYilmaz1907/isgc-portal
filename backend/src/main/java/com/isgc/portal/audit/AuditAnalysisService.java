package com.isgc.portal.audit;

import com.isgc.portal.audit.dto.AuditAnalysisResponse;
import com.isgc.portal.audit.dto.AuditCategoryCompliance;
import com.isgc.portal.audit.dto.AuditItemResultResponse;
import com.isgc.portal.security.CurrentUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditAnalysisService {
  private final AuditRepository auditRepo;
  private final ChecklistItemRepository itemRepo;
  private final AuditItemResultRepository resultRepo;
  private final AuditService auditService;

  public AuditAnalysisService(
      AuditRepository auditRepo,
      ChecklistItemRepository itemRepo,
      AuditItemResultRepository resultRepo,
      AuditService auditService
  ) {
    this.auditRepo = auditRepo;
    this.itemRepo = itemRepo;
    this.resultRepo = resultRepo;
    this.auditService = auditService;
  }

  @Transactional(readOnly = true)
  public List<AuditItemResultResponse> results(CurrentUser user, UUID auditId) {
    auditService.get(user, auditId);
    Audit a = auditRepo.findById(auditId).orElseThrow();
    if (a.getChecklist() == null) return List.of();

    var items = itemRepo.findByChecklistId(a.getChecklist().getId());
    var saved = resultRepo.findByAuditId(auditId);
    return items.stream()
        .filter(ChecklistItem::isEnabled)
        .sorted(Comparator.comparingInt(ChecklistItem::getItemNo))
        .map(ci -> {
          var r = saved.stream()
              .filter(x -> x.getChecklistItem().getId().equals(ci.getId()))
              .findFirst()
              .orElse(null);
          return new AuditItemResultResponse(
              ci.getId(),
              ci.getItemNo(),
              ci.getCategoryNo(),
              ci.getQuestion(),
              r != null ? r.getScore() : BigDecimal.ZERO,
              ci.getMaxScore(),
              r == null || r.isApplicable(),
              r != null ? r.getNote() : null
          );
        })
        .toList();
  }

  @Transactional(readOnly = true)
  public AuditAnalysisResponse analysis(CurrentUser user, UUID auditId) {
    auditService.get(user, auditId);
    Audit a = auditRepo.findById(auditId).orElseThrow();
    if (a.getChecklist() == null) {
      return new AuditAnalysisResponse(BigDecimal.ZERO, "red", List.of(), List.of());
    }

    var items = itemRepo.findByChecklistId(a.getChecklist().getId()).stream()
        .filter(ChecklistItem::isEnabled)
        .sorted(Comparator.comparingInt(ChecklistItem::getItemNo))
        .toList();
    var saved = resultRepo.findByAuditId(auditId);
    Set<Integer> parentsWithChildren = categoriesWithChildren(items);

    List<AuditAnalysisResponse.AuditItemCompliance> itemRows = new ArrayList<>();
    Map<Integer, CategoryAccumulator> categoryMap = new HashMap<>();

    for (ChecklistItem ci : items) {
      if (parentsWithChildren.contains(ci.getItemNo())) continue;

      AuditItemResult matched = saved.stream()
          .filter(r -> r.getChecklistItem().getId().equals(ci.getId()))
          .findFirst()
          .orElse(null);
      if (matched != null && !matched.isApplicable()) continue;

      BigDecimal score = matched != null ? matched.getScore() : BigDecimal.ZERO;
      BigDecimal pct = AuditComplianceUtil.compliancePercent(score, ci.getMaxScore());
      String zone = AuditComplianceUtil.colorZone(pct);

      itemRows.add(new AuditAnalysisResponse.AuditItemCompliance(
          ci.getItemNo(),
          ci.getCategoryNo(),
          ci.getQuestion(),
          pct,
          zone,
          true,
          score,
          ci.getMaxScore()
      ));

      int catNo = ci.getCategoryNo();
      categoryMap
          .computeIfAbsent(catNo, k -> new CategoryAccumulator(k, categoryLabel(items, k)))
          .add(ci, score);
    }

    List<AuditCategoryCompliance> categories = categoryMap.values().stream()
        .map(CategoryAccumulator::toDto)
        .sorted(Comparator.comparingInt(AuditCategoryCompliance::categoryNo))
        .toList();

    BigDecimal overall = a.getCalculatedScore() != null ? a.getCalculatedScore() : BigDecimal.ZERO;
    return new AuditAnalysisResponse(
        overall,
        AuditComplianceUtil.colorZone(overall),
        categories,
        itemRows
    );
  }

  static Set<Integer> categoriesWithChildren(List<ChecklistItem> items) {
    Set<Integer> cats = new HashSet<>();
    for (ChecklistItem ci : items) {
      if (ci.getCategoryNo() != ci.getItemNo()) {
        cats.add(ci.getCategoryNo());
      }
    }
    return cats;
  }

  private static String categoryLabel(List<ChecklistItem> items, int categoryNo) {
    return items.stream()
        .filter(ci -> ci.getItemNo() == categoryNo)
        .map(ChecklistItem::getQuestion)
        .findFirst()
        .orElse("Kategori " + categoryNo);
  }

  private static final class CategoryAccumulator {
    private final int categoryNo;
    private final String label;
    private BigDecimal numer = BigDecimal.ZERO;
    private BigDecimal denom = BigDecimal.ZERO;
    private int itemCount;

    CategoryAccumulator(int categoryNo, String label) {
      this.categoryNo = categoryNo;
      this.label = label;
    }

    void add(ChecklistItem ci, BigDecimal score) {
      itemCount++;
      BigDecimal w = ci.getWeight();
      denom = denom.add(ci.getMaxScore().multiply(w));
      numer = numer.add(score.multiply(w));
    }

    AuditCategoryCompliance toDto() {
      BigDecimal pct = denom.compareTo(BigDecimal.ZERO) == 0
          ? BigDecimal.ZERO
          : numer.multiply(BigDecimal.valueOf(100)).divide(denom, 2, RoundingMode.HALF_UP);
      return new AuditCategoryCompliance(
          categoryNo,
          label,
          pct,
          AuditComplianceUtil.colorZone(pct),
          itemCount,
          itemCount
      );
    }
  }
}
