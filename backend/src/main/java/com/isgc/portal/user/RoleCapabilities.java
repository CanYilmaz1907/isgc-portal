package com.isgc.portal.user;

public final class RoleCapabilities {
  private RoleCapabilities() {}

  /** HSE Yöneticisi / Saha İSG — tam yazma yetkisi */
  public static boolean canWrite(Role role) {
    return role == Role.ADMIN || role == Role.ISG_C;
  }

  /** Salt okunur + ISG — tüm kayıtları görüntüleme */
  public static boolean canViewAll(Role role) {
    return role == Role.ADMIN || role == Role.ISG_C || role == Role.READ_ONLY;
  }

  public static boolean isReadOnly(Role role) {
    return role == Role.READ_ONLY;
  }
}
