package com.isgc.portal.discipline;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DisciplineJob {
  private final DisciplineService service;

  public DisciplineJob(DisciplineService service) {
    this.service = service;
  }

  @Scheduled(cron = "0 30 9 * * *")
  public void dailySummary() {
    service.sendDailySummary();
  }
}


