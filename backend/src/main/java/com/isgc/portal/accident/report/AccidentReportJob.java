package com.isgc.portal.accident.report;

import com.isgc.portal.accident.report.dto.AccidentDistributionResponse;
import com.isgc.portal.mail.MailService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AccidentReportJob {
  private final AccidentReportSubscriptionRepository repo;
  private final AccidentReportingService reporting;
  private final MailService mailService;

  public AccidentReportJob(
      AccidentReportSubscriptionRepository repo,
      AccidentReportingService reporting,
      MailService mailService
  ) {
    this.repo = repo;
    this.reporting = reporting;
    this.mailService = mailService;
  }

  @Scheduled(cron = "0 */5 * * * *")
  @Transactional
  public void tick() {
    // Check every 5 min; send when due (per subscription local time)
    for (AccidentReportSubscription s : repo.findEnabled()) {
      if (!isDueNow(s)) continue;
      if (s.getLastSentAt() != null && s.getLastSentAt().isAfter(Instant.now().minusSeconds(60 * 10))) {
        continue; // safety
      }

      int periodDays = parsePeriodDays(s.getFilters());
      Instant to = Instant.now();
      Instant from = Instant.now().minusSeconds((long) periodDays * 24 * 3600);
      var projectId = s.getProject() != null ? s.getProject().getId() : null;

      AccidentDistributionResponse dist = reporting.distribution(from, to, projectId, null, null);
      String subtitle = "Son " + periodDays + " gün";

      var toList = mailService.parseEmails(s.getToEmails());
      var ccList = mailService.parseEmails(s.getCcEmails());

      mailService.sendTemplate(
          "Kaza Raporu (" + s.getFrequency().name() + ")",
          toList,
          ccList,
          "mail/accident-report",
          Map.of(
              "subtitle", subtitle,
              "byClass", dist.byAccidentClass().toString(),
              "byPotential", dist.byPotentialLevel().toString()
          )
      );

      s.setLastSentAt(Instant.now());
      repo.save(s);
    }
  }

  private static boolean isDueNow(AccidentReportSubscription s) {
    ZoneId zone = ZoneId.systemDefault();
    LocalDateTime now = LocalDateTime.now(zone);
    if (now.getHour() != s.getHourOfDay()) return false;
    if (Math.abs(now.getMinute() - s.getMinuteOfHour()) > 2) return false; // within 2 min window

    Instant last = s.getLastSentAt();
    if (last == null) return true;
    LocalDate lastDate = LocalDateTime.ofInstant(last, zone).toLocalDate();
    LocalDate today = now.toLocalDate();

    return switch (s.getFrequency()) {
      case DAILY -> !lastDate.equals(today);
      case WEEKLY -> today.getDayOfWeek().getValue() == 1 && !lastDate.equals(today); // Monday
      case MONTHLY -> today.getDayOfMonth() == 1 && !lastDate.equals(today);
    };
  }

  private static int parsePeriodDays(String filtersJson) {
    if (filtersJson == null || filtersJson.isBlank()) return 30;
    // tiny parse to avoid JSON dependency: look for "periodDays": number
    try {
      String s = filtersJson;
      int i = s.indexOf("periodDays");
      if (i < 0) return 30;
      int colon = s.indexOf(':', i);
      if (colon < 0) return 30;
      int j = colon + 1;
      while (j < s.length() && !Character.isDigit(s.charAt(j))) j++;
      int k = j;
      while (k < s.length() && Character.isDigit(s.charAt(k))) k++;
      if (k <= j) return 30;
      int v = Integer.parseInt(s.substring(j, k));
      if (v < 1) return 30;
      if (v > 365) return 365;
      return v;
    } catch (Exception e) {
      return 30;
    }
  }
}


