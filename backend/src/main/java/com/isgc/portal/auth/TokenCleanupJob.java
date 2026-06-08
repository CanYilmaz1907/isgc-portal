package com.isgc.portal.auth;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenCleanupJob {
  private final AuthService authService;

  public TokenCleanupJob(AuthService authService) {
    this.authService = authService;
  }

  @Scheduled(cron = "0 0 3 * * *")
  public void cleanup() {
    authService.cleanupExpiredTokens();
  }
}


