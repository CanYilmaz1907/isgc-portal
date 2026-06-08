package com.isgc.portal.audit;

public final class ChecklistCategoryUtil {
  private ChecklistCategoryUtil() {}

  public static int inferCategoryNo(int itemNo) {
    if (itemNo >= 101 && itemNo <= 199) {
      return 100;
    }
    if (itemNo >= 1000) {
      return itemNo / 10;
    }
    return itemNo;
  }

  public static int resolveCategoryNo(int itemNo, Integer categoryNo) {
    return categoryNo != null ? categoryNo : inferCategoryNo(itemNo);
  }
}
