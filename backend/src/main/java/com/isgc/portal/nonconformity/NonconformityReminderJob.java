package com.isgc.portal.nonconformity;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NonconformityReminderJob {
  private final NonconformityService service;

  public NonconformityReminderJob(NonconformityService service) {
    this.service = service;
  }

  @Scheduled(cron = "0 0 9 * * *")
  public void dailyReminders() {
    service.sendOpenReminders();
  }
}


