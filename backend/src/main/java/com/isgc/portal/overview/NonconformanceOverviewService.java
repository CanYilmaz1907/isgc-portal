package com.isgc.portal.overview;

import com.isgc.portal.audit.Audit;
import com.isgc.portal.audit.AuditRepository;
import com.isgc.portal.audit.AuditStatus;
import com.isgc.portal.audit.AuditAnalysisService;
import com.isgc.portal.audit.dto.AuditAnalysisResponse;
import com.isgc.portal.discipline.DisciplineLogRepository;
import com.isgc.portal.ncr.NcrRepository;
import com.isgc.portal.ncr.NcrStatus;
import com.isgc.portal.nonconformity.NonconformityRepository;
import com.isgc.portal.nonconformity.NonconformityStatus;
import com.isgc.portal.ncr.Ncr;
import com.isgc.portal.overview.dto.AuditCompareResponse;
import com.isgc.portal.overview.dto.NonconformanceOverviewResponse;
import com.isgc.portal.security.CurrentUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NonconformanceOverviewService {
  private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
  private static final int TREND_MONTHS = 6;

  private final NcrRepository ncrRepo;
  private final DisciplineLogRepository disciplineRepo;
  private final NonconformityRepository nonconformityRepo;
  private final AuditRepository auditRepo;
  private final AuditAnalysisService auditAnalysisService;

  public NonconformanceOverviewService(
      NcrRepository ncrRepo,
      DisciplineLogRepository disciplineRepo,
      NonconformityRepository nonconformityRepo,
      AuditRepository auditRepo,
      AuditAnalysisService auditAnalysisService
  ) {
    this.ncrRepo = ncrRepo;
    this.disciplineRepo = disciplineRepo;
    this.nonconformityRepo = nonconformityRepo;
    this.auditRepo = auditRepo;
    this.auditAnalysisService = auditAnalysisService;
  }

  @Transactional(readOnly = true)
  public NonconformanceOverviewResponse overview(CurrentUser user) {
    var ncrs = ncrRepo.findAll();
    var discipline = disciplineRepo.findAll();
    var legacy = nonconformityRepo.findAll();
    var audits = auditRepo.findAll().stream()
        .filter(a -> a.getStatus() == AuditStatus.COMPLETED)
        .toList();

    Map<String, Long> moduleTotals = Map.of(
        "NCR", (long) ncrs.size(),
        "DISCIPLINE", (long) discipline.size(),
        "LEGACY_NC", (long) legacy.size(),
        "AUDITS", (long) audits.size()
    );

    long openNcr = ncrs.stream().filter(n -> n.getStatus() != NcrStatus.CLOSED).count();
    long openLegacy = legacy.stream().filter(n -> n.getStatus() == NonconformityStatus.OPEN).count();
    long overdueNcr = ncrs.stream().filter(this::isNcrOverdue).count();

    Map<String, Long> openByModule = Map.of(
        "NCR", openNcr,
        "DISCIPLINE", discipline.stream().filter(d -> d.getStatus() != null).count(),
        "LEGACY_NC", openLegacy
    );

    Map<String, Long> ncrByStatus = new HashMap<>();
    for (var n : ncrs) {
      String key = n.getStatus() != null ? n.getStatus().name() : "UNKNOWN";
      ncrByStatus.put(key, ncrByStatus.getOrDefault(key, 0L) + 1L);
    }

    BigDecimal avgAudit = audits.stream()
        .map(Audit::getCalculatedScore)
        .filter(s -> s != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (!audits.isEmpty()) {
      avgAudit = avgAudit.divide(BigDecimal.valueOf(audits.size()), 2, RoundingMode.HALF_UP);
    } else {
      avgAudit = BigDecimal.ZERO;
    }

    Map<String, Map<String, Long>> monthlyTrend = new LinkedHashMap<>();
    monthlyTrend.put("NCR", monthBuckets(ncrs.stream().map(n -> n.getNcrDate()).toList()));
    monthlyTrend.put("DISCIPLINE", monthBuckets(discipline.stream()
        .map(d -> d.getOccurredAt() != null ? d.getOccurredAt().atZone(ZoneId.systemDefault()).toLocalDate() : null)
        .toList()));
    monthlyTrend.put("LEGACY_NC", monthBuckets(legacy.stream()
        .map(n -> n.getDueDate())
        .toList()));

    List<NonconformanceOverviewResponse.AuditScorePoint> recentAudits = audits.stream()
        .sorted(Comparator.comparing(Audit::getFinishedAt, Comparator.nullsLast(Comparator.reverseOrder())))
        .limit(8)
        .map(a -> new NonconformanceOverviewResponse.AuditScorePoint(
            a.getId().toString(),
            a.getTitle(),
            a.getProject() != null ? a.getProject().getName() : null,
            a.getCalculatedScore(),
            a.getFinishedAt() != null ? a.getFinishedAt().toString() : null
        ))
        .toList();

    return new NonconformanceOverviewResponse(
        moduleTotals,
        openByModule,
        overdueNcr,
        avgAudit,
        ncrByStatus,
        monthlyTrend,
        recentAudits
    );
  }

  @Transactional(readOnly = true)
  public AuditCompareResponse compare(CurrentUser user, UUID leftId, UUID rightId) {
    AuditAnalysisResponse left = auditAnalysisService.analysis(user, leftId);
    AuditAnalysisResponse right = auditAnalysisService.analysis(user, rightId);
    var leftAudit = auditRepo.findById(leftId).orElseThrow();
    var rightAudit = auditRepo.findById(rightId).orElseThrow();

    Map<Integer, BigDecimal> leftMap = new HashMap<>();
    left.categories().forEach(c -> leftMap.put(c.categoryNo(), c.compliancePercent()));
    Map<Integer, BigDecimal> rightMap = new HashMap<>();
    right.categories().forEach(c -> rightMap.put(c.categoryNo(), c.compliancePercent()));

    var allCats = new java.util.HashSet<Integer>();
    allCats.addAll(leftMap.keySet());
    allCats.addAll(rightMap.keySet());

    String leftLabel = leftAudit.getTitle();
    String rightLabel = rightAudit.getTitle();

    List<AuditCompareResponse.CategoryDelta> deltas = allCats.stream()
        .sorted()
        .map(catNo -> {
          BigDecimal lp = leftMap.getOrDefault(catNo, BigDecimal.ZERO);
          BigDecimal rp = rightMap.getOrDefault(catNo, BigDecimal.ZERO);
          String label = left.categories().stream()
              .filter(c -> c.categoryNo() == catNo)
              .map(c -> c.label())
              .findFirst()
              .orElseGet(() -> right.categories().stream()
                  .filter(c -> c.categoryNo() == catNo)
                  .map(c -> c.label())
                  .findFirst()
                  .orElse("Kategori " + catNo));
          return new AuditCompareResponse.CategoryDelta(catNo, label, lp, rp, lp.subtract(rp));
        })
        .toList();

    return new AuditCompareResponse(
        leftId.toString(),
        leftLabel,
        rightId.toString(),
        rightLabel,
        left,
        right,
        deltas
    );
  }

  private Map<String, Long> monthBuckets(List<LocalDate> dates) {
    List<String> months = lastMonths(TREND_MONTHS);
    Map<String, Long> out = new LinkedHashMap<>();
    for (String m : months) out.put(m, 0L);
    for (LocalDate d : dates) {
      if (d == null) continue;
      String key = d.format(MONTH_FMT);
      if (out.containsKey(key)) out.put(key, out.get(key) + 1L);
    }
    return out;
  }

  private boolean isNcrOverdue(Ncr n) {
    if (n.getStatus() == NcrStatus.CLOSED) return false;
    LocalDate target = n.getTargetCompletionDate() != null ? n.getTargetCompletionDate() : n.getDueDate();
    return target != null && target.isBefore(LocalDate.now());
  }

  private static List<String> lastMonths(int count) {
    List<String> months = new ArrayList<>();
    LocalDate now = LocalDate.now();
    for (int i = count - 1; i >= 0; i--) {
      months.add(now.minusMonths(i).format(MONTH_FMT));
    }
    return months;
  }
}
