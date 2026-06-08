package com.isgc.portal.common;

import com.isgc.portal.security.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {
  private SecurityUtil() {}

  public static CurrentUser requireCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof CurrentUser cu)) {
      throw new IllegalStateException("No authenticated user");
    }
    return cu;
  }
}


