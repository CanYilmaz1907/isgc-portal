package com.isgc.portal.accident.report;

import com.isgc.portal.accident.report.dto.AccidentDistributionResponse;
import com.isgc.portal.accident.report.dto.AccidentSeriesPoint;
import com.isgc.portal.accident.report.dto.AccidentRootCausePoint;
import com.isgc.portal.accident.report.dto.AccidentStatsSummaryResponse;
import com.isgc.portal.accident.report.dto.AccidentDashboardResponse;
import com.isgc.portal.accident.report.dto.AccidentKpiResponse;
import com.isgc.portal.accident.report.dto.LabelCountPoint;
import com.isgc.portal.accident.report.dto.CauseGroupCountPoint;
import com.isgc.portal.accident.report.dto.CauseSummaryRow;
import com.isgc.portal.accident.report.dto.AccidentMonthlyStackPoint;
import com.isgc.portal.accident.CauseCategory;
import com.isgc.portal.accident.CauseCategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AccidentReportingService {
  private final EntityManager em;
  private final CauseCategoryRepository causeCategoryRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AccidentReportingService(EntityManager em, CauseCategoryRepository causeCategoryRepository) {
    this.em = em;
    this.causeCategoryRepository = causeCategoryRepository;
  }

  public List<AccidentSeriesPoint> series(
      Instant from,
      Instant to,
      UUID projectId,
      String area,
      UUID accidentTypeId,
      String bucket
  ) {
    try {
      // Validate and sanitize bucket parameter
      String trunc;
      if ("DAY".equalsIgnoreCase(bucket)) trunc = "day";
      else if ("WEEK".equalsIgnoreCase(bucket)) trunc = "week";
      else if ("MONTH".equalsIgnoreCase(bucket)) trunc = "month";
      else trunc = "month"; // default

      // Build SQL dynamically to avoid PostgreSQL parameter type inference issues
      StringBuilder sqlBuilder = new StringBuilder();
      sqlBuilder.append(String.format("""
          select date_trunc('%s', a.occurred_at) as b, count(*) as c
          from accidents a
          where a.occurred_at is not null
          """, trunc));
      
      if (projectId != null) {
        sqlBuilder.append(" and a.project_id = :projectId");
      }
      if (area != null && !area.isBlank()) {
        sqlBuilder.append(" and a.area = :area");
      }
      if (accidentTypeId != null) {
        sqlBuilder.append(" and a.accident_type_id = :accidentTypeId");
      }
      if (from != null) {
        sqlBuilder.append(" and a.occurred_at >= :from");
      }
      if (to != null) {
        sqlBuilder.append(" and a.occurred_at <= :to");
      }
      
      sqlBuilder.append("""
          
          group by b
          order by b
          """);
      
      Query q = em.createNativeQuery(sqlBuilder.toString());
      if (projectId != null) {
        q.setParameter("projectId", projectId);
      }
      if (area != null && !area.isBlank()) {
        q.setParameter("area", area);
      }
      if (accidentTypeId != null) {
        q.setParameter("accidentTypeId", accidentTypeId);
      }
      if (from != null) {
        q.setParameter("from", Timestamp.from(from));
      }
      if (to != null) {
        q.setParameter("to", Timestamp.from(to));
      }

      @SuppressWarnings("unchecked")
      List<Object[]> rows = q.getResultList();
      return rows.stream()
          .filter(r -> r[0] != null && r[1] != null)
          .map(r -> {
            Instant instant;
            if (r[0] instanceof Instant) {
              instant = (Instant) r[0];
            } else if (r[0] instanceof Timestamp) {
              instant = ((Timestamp) r[0]).toInstant();
            } else {
              throw new RuntimeException("Unexpected type for date_trunc result: " + r[0].getClass());
            }
            return new AccidentSeriesPoint(instant, ((Number) r[1]).longValue());
          })
          .toList();
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate accident series: " + e.getMessage(), e);
    }
  }

  public AccidentDistributionResponse distribution(
      Instant from,
      Instant to,
      UUID projectId,
      String area,
      UUID accidentTypeId
  ) {
    try {
      Map<String, Long> byClass = groupCount("accident_class", from, to, projectId, area, accidentTypeId);
      Map<String, Long> byPotential = groupCount("potential_level", from, to, projectId, area, accidentTypeId);
      return new AccidentDistributionResponse(byClass, byPotential);
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate accident distribution: " + e.getMessage(), e);
    }
  }

  private Map<String, Long> groupCount(
      String column,
      Instant from,
      Instant to,
      UUID projectId,
      String area,
      UUID accidentTypeId
  ) {
    // Validate column name to prevent SQL injection
    if (!column.equals("accident_class") && !column.equals("potential_level")) {
      throw new IllegalArgumentException("Invalid column: " + column);
    }
    
    // Build SQL dynamically to avoid PostgreSQL parameter type inference issues
    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append(String.format("""
        select a.%s as k, count(*) as c
        from accidents a
        where 1=1
        """, column));
    
    if (projectId != null) {
      sqlBuilder.append(" and a.project_id = :projectId");
    }
    if (area != null && !area.isBlank()) {
      sqlBuilder.append(" and a.area = :area");
    }
    if (accidentTypeId != null) {
      sqlBuilder.append(" and a.accident_type_id = :accidentTypeId");
    }
    if (from != null) {
      sqlBuilder.append(" and a.occurred_at >= :from");
    }
    if (to != null) {
      sqlBuilder.append(" and a.occurred_at <= :to");
    }
    
    sqlBuilder.append("""
        
        group by k
        """);
    
    Query q = em.createNativeQuery(sqlBuilder.toString());
    if (projectId != null) {
      q.setParameter("projectId", projectId);
    }
    if (area != null && !area.isBlank()) {
      q.setParameter("area", area);
    }
    if (accidentTypeId != null) {
      q.setParameter("accidentTypeId", accidentTypeId);
    }
    if (from != null) {
      q.setParameter("from", Timestamp.from(from));
    }
    if (to != null) {
      q.setParameter("to", Timestamp.from(to));
    }

    @SuppressWarnings("unchecked")
    List<Object[]> rows = q.getResultList();
    Map<String, Long> out = new HashMap<>();
    for (Object[] r : rows) {
      if (r[1] != null) {
        String k = r[0] != null ? r[0].toString() : "Tanımsız";
        out.put(k, ((Number) r[1]).longValue());
      }
    }
    return out;
  }

  public AccidentStatsSummaryResponse summary(
      Instant from,
      Instant to,
      UUID projectId,
      String area,
      UUID accidentTypeId
  ) {
    Map<String, Long> byClassification = classificationCounts(from, to, projectId, area, accidentTypeId);
    long total = byClassification.values().stream().mapToLong(Long::longValue).sum();
    if (total == 0) {
      Map<String, Long> byClass = groupCount("accident_class", from, to, projectId, area, accidentTypeId);
      total = byClass.values().stream().mapToLong(Long::longValue).sum();
      long nearMiss = byClass.getOrDefault("NEAR_MISS", 0L);
      long lti = byClass.getOrDefault("MAJOR", 0L);
      long fat = byClass.getOrDefault("FATAL", 0L);
      return new AccidentStatsSummaryResponse(total, lti, fat, nearMiss);
    }
    long nearMiss = byClassification.getOrDefault("NEAR_MISS", 0L);
    long lti = byClassification.getOrDefault("LTI", 0L);
    long fat = byClassification.getOrDefault("FAT", 0L);
    return new AccidentStatsSummaryResponse(total, lti, fat, nearMiss);
  }

  public AccidentDashboardResponse dashboard(
      Instant from,
      Instant to,
      UUID projectId,
      String area,
      UUID accidentTypeId
  ) {
    Map<String, Long> byClassification = classificationCounts(from, to, projectId, area, accidentTypeId);
    long total = byClassification.values().stream().mapToLong(Long::longValue).sum();
    AccidentKpiResponse kpis = new AccidentKpiResponse(
        total,
        byClassification.getOrDefault("FAT", 0L),
        byClassification.getOrDefault("LTI", 0L),
        byClassification.getOrDefault("MTC", 0L),
        byClassification.getOrDefault("FAC", 0L),
        byClassification.getOrDefault("NEAR_MISS", 0L),
        byClassification.getOrDefault("EQUIPMENT", 0L)
    );

    return new AccidentDashboardResponse(
        kpis,
        monthlyTrend(from, to, projectId, area, accidentTypeId),
        byClassification,
        labelCounts("hazard_source", from, to, projectId, area, accidentTypeId),
        causeGroupCounts("DIRECT", from, to, projectId, area, accidentTypeId),
        causeGroupCounts("ROOT", from, to, projectId, area, accidentTypeId),
        labelCounts("injured_body_part", from, to, projectId, area, accidentTypeId),
        labelCountsMap("time_period", from, to, projectId, area, accidentTypeId),
        labelCountsMap("area", from, to, projectId, area, accidentTypeId),
        causeSummary("DIRECT", from, to, projectId, area, accidentTypeId),
        causeSummary("ROOT", from, to, projectId, area, accidentTypeId)
    );
  }

  private Map<String, Long> classificationCounts(
      Instant from, Instant to, UUID projectId, String area, UUID accidentTypeId
  ) {
    StringBuilder sql = new StringBuilder("""
        select coalesce(a.classification, a.accident_class::text) as k, count(*) as c
        from accidents a
        where 1=1
        """);
    appendFilters(sql, projectId, area, accidentTypeId, from, to);
    sql.append(" group by k");
    return runCountMap(sql.toString(), projectId, area, accidentTypeId, from, to);
  }

  private List<AccidentMonthlyStackPoint> monthlyTrend(
      Instant from, Instant to, UUID projectId, String area, UUID accidentTypeId
  ) {
    StringBuilder sql = new StringBuilder("""
        select date_trunc('month', a.occurred_at) as m,
               coalesce(a.classification, a.accident_class::text) as k,
               count(*) as c
        from accidents a
        where a.occurred_at is not null
        """);
    appendFilters(sql, projectId, area, accidentTypeId, from, to);
    sql.append(" group by m, k order by m");

    Query q = em.createNativeQuery(sql.toString());
    bindFilters(q, projectId, area, accidentTypeId, from, to);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = q.getResultList();

    Map<String, Map<String, Long>> byMonth = new LinkedHashMap<>();
    for (Object[] r : rows) {
      if (r[0] == null || r[1] == null) continue;
      String month = formatMonth(r[0]);
      String key = r[1].toString();
      long count = ((Number) r[2]).longValue();
      byMonth.computeIfAbsent(month, k -> new LinkedHashMap<>()).merge(key, count, Long::sum);
    }
    return byMonth.entrySet().stream()
        .map(e -> new AccidentMonthlyStackPoint(e.getKey(), e.getValue()))
        .toList();
  }

  private String formatMonth(Object value) {
    Instant instant;
    if (value instanceof Instant i) instant = i;
    else if (value instanceof Timestamp ts) instant = ts.toInstant();
    else return value.toString();
    return instant.atZone(java.time.ZoneId.systemDefault()).format(
        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
  }

  private List<LabelCountPoint> labelCounts(
      String column, Instant from, Instant to, UUID projectId, String area, UUID accidentTypeId
  ) {
    validateLabelColumn(column);
    StringBuilder sql = new StringBuilder(String.format("""
        select a.%s as k, count(*) as c
        from accidents a
        where a.%s is not null and a.%s <> ''
        """, column, column, column));
    appendFilters(sql, projectId, area, accidentTypeId, from, to);
    sql.append(" group by k order by c desc");

    Query q = em.createNativeQuery(sql.toString());
    bindFilters(q, projectId, area, accidentTypeId, from, to);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = q.getResultList();
    return rows.stream()
        .filter(r -> r[0] != null && r[1] != null)
        .map(r -> new LabelCountPoint(r[0].toString(), ((Number) r[1]).longValue()))
        .toList();
  }

  private Map<String, Long> labelCountsMap(
      String column, Instant from, Instant to, UUID projectId, String area, UUID accidentTypeId
  ) {
    return labelCounts(column, from, to, projectId, area, accidentTypeId).stream()
        .collect(Collectors.toMap(LabelCountPoint::label, LabelCountPoint::count, Long::sum, LinkedHashMap::new));
  }

  private List<CauseGroupCountPoint> causeGroupCounts(
      String causeType, Instant from, Instant to, UUID projectId, String area, UUID accidentTypeId
  ) {
    String table = "DIRECT".equals(causeType) ? "accident_direct_causes" : "accident_root_causes";
    StringBuilder sql = new StringBuilder(String.format("""
        select split_part(c.cause_code, '-', 1) as grp, count(*) as c
        from %s c
        join accidents a on a.id = c.accident_id
        where 1=1
        """, table));
    appendFilters(sql, projectId, area, accidentTypeId, from, to);
    sql.append(" group by grp order by grp");

    Query q = em.createNativeQuery(sql.toString());
    bindFilters(q, projectId, area, accidentTypeId, from, to);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = q.getResultList();

    Map<String, CauseCategory> groupMeta = new HashMap<>();
    for (CauseCategory cat : causeCategoryRepository.findByCauseTypeAndEnabledTrueOrderBySortOrderAsc(causeType)) {
      groupMeta.putIfAbsent(cat.getGroupCode(), cat);
    }

    return rows.stream()
        .filter(r -> r[0] != null && r[1] != null)
        .map(r -> {
          String groupCode = r[0].toString();
          CauseCategory meta = groupMeta.get(groupCode);
          return new CauseGroupCountPoint(
              groupCode,
              meta != null ? meta.getGroupName() : "Grup " + groupCode,
              meta != null ? meta.getSection() : "",
              ((Number) r[1]).longValue()
          );
        })
        .toList();
  }

  private List<CauseSummaryRow> causeSummary(
      String causeType, Instant from, Instant to, UUID projectId, String area, UUID accidentTypeId
  ) {
    String table = "DIRECT".equals(causeType) ? "accident_direct_causes" : "accident_root_causes";
    StringBuilder sql = new StringBuilder(String.format("""
        select split_part(c.cause_code, '-', 1) as grp,
               c.cause_code,
               c.cause_label,
               count(*) as c
        from %s c
        join accidents a on a.id = c.accident_id
        where 1=1
        """, table));
    appendFilters(sql, projectId, area, accidentTypeId, from, to);
    sql.append(" group by grp, c.cause_code, c.cause_label order by grp, c desc");

    Query q = em.createNativeQuery(sql.toString());
    bindFilters(q, projectId, area, accidentTypeId, from, to);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = q.getResultList();

    Map<String, CauseCategory> groupMeta = new HashMap<>();
    for (CauseCategory cat : causeCategoryRepository.findByCauseTypeAndEnabledTrueOrderBySortOrderAsc(causeType)) {
      groupMeta.putIfAbsent(cat.getGroupCode(), cat);
    }

    Map<String, Long> totals = new LinkedHashMap<>();
    Map<String, String> topCode = new HashMap<>();
    Map<String, String> topLabel = new HashMap<>();
    Map<String, Long> topCount = new HashMap<>();

    for (Object[] r : rows) {
      if (r[0] == null || r[3] == null) continue;
      String grp = r[0].toString();
      String code = r[1] != null ? r[1].toString() : "";
      String label = r[2] != null ? r[2].toString() : code;
      long count = ((Number) r[3]).longValue();
      totals.merge(grp, count, Long::sum);
      if (!topCount.containsKey(grp) || count > topCount.get(grp)) {
        topCode.put(grp, code);
        topLabel.put(grp, label);
        topCount.put(grp, count);
      }
    }

    return totals.entrySet().stream()
        .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey())))
        .map(e -> {
          String grp = e.getKey();
          CauseCategory meta = groupMeta.get(grp);
          String category = meta != null ? grp + ". " + meta.getGroupName() : "Grup " + grp;
          return new CauseSummaryRow(
              category,
              e.getValue(),
              topCode.getOrDefault(grp, ""),
              topLabel.getOrDefault(grp, ""),
              topCount.getOrDefault(grp, 0L)
          );
        })
        .toList();
  }

  private Map<String, Long> runCountMap(
      String sql, UUID projectId, String area, UUID accidentTypeId, Instant from, Instant to
  ) {
    Query q = em.createNativeQuery(sql);
    bindFilters(q, projectId, area, accidentTypeId, from, to);
    @SuppressWarnings("unchecked")
    List<Object[]> rows = q.getResultList();
    Map<String, Long> out = new LinkedHashMap<>();
    for (Object[] r : rows) {
      if (r[1] != null) {
        out.put(r[0] != null ? r[0].toString() : "UNKNOWN", ((Number) r[1]).longValue());
      }
    }
    return out;
  }

  private void appendFilters(
      StringBuilder sql, UUID projectId, String area, UUID accidentTypeId, Instant from, Instant to
  ) {
    if (projectId != null) sql.append(" and a.project_id = :projectId");
    if (area != null && !area.isBlank()) sql.append(" and a.area = :area");
    if (accidentTypeId != null) sql.append(" and a.accident_type_id = :accidentTypeId");
    if (from != null) sql.append(" and a.occurred_at >= :from");
    if (to != null) sql.append(" and a.occurred_at <= :to");
  }

  private void bindFilters(
      Query q, UUID projectId, String area, UUID accidentTypeId, Instant from, Instant to
  ) {
    if (projectId != null) q.setParameter("projectId", projectId);
    if (area != null && !area.isBlank()) q.setParameter("area", area);
    if (accidentTypeId != null) q.setParameter("accidentTypeId", accidentTypeId);
    if (from != null) q.setParameter("from", Timestamp.from(from));
    if (to != null) q.setParameter("to", Timestamp.from(to));
  }

  private void validateLabelColumn(String column) {
    if (!column.equals("hazard_source") && !column.equals("injured_body_part")
        && !column.equals("time_period") && !column.equals("area")) {
      throw new IllegalArgumentException("Invalid column: " + column);
    }
  }

  public List<AccidentRootCausePoint> rootCauses(
      Instant from,
      Instant to,
      UUID projectId,
      String area,
      UUID accidentTypeId,
      int limit
  ) {
    try {
      StringBuilder sql = new StringBuilder();
      sql.append("""
          select a.root_cause_data
          from accidents a
          where 1=1
          """);
      if (projectId != null) sql.append(" and a.project_id = :projectId");
      if (area != null && !area.isBlank()) sql.append(" and a.area = :area");
      if (accidentTypeId != null) sql.append(" and a.accident_type_id = :accidentTypeId");
      if (from != null) sql.append(" and a.occurred_at >= :from");
      if (to != null) sql.append(" and a.occurred_at <= :to");

      Query q = em.createNativeQuery(sql.toString());
      if (projectId != null) q.setParameter("projectId", projectId);
      if (area != null && !area.isBlank()) q.setParameter("area", area);
      if (accidentTypeId != null) q.setParameter("accidentTypeId", accidentTypeId);
      if (from != null) q.setParameter("from", Timestamp.from(from));
      if (to != null) q.setParameter("to", Timestamp.from(to));

      @SuppressWarnings("unchecked")
      List<Object> rows = q.getResultList();
      Map<String, Long> counts = new HashMap<>();

      for (Object row : rows) {
        if (row == null) continue;
        String json = row.toString();
        if (json.isBlank() || "{}".equals(json)) continue;
        JsonNode root = objectMapper.readTree(json);

        countArray(root, "personalFactors", counts);
        countArray(root, "workFactors", counts);
      }

      return counts.entrySet().stream()
          .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
          .limit(Math.max(1, limit))
          .map(e -> new AccidentRootCausePoint(e.getKey(), e.getValue()))
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Failed to compute root causes: " + e.getMessage(), e);
    }
  }

  private void countArray(JsonNode root, String field, Map<String, Long> counts) {
    JsonNode arr = root.get(field);
    if (arr == null || !arr.isArray()) return;
    for (JsonNode item : arr) {
      if (item == null || item.isNull()) continue;
      String val = item.asText();
      if (val == null || val.isBlank()) continue;
      counts.put(val, counts.getOrDefault(val, 0L) + 1L);
    }
  }
}


