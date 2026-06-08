package com.isgc.portal.document.dto;

import java.util.List;
import java.util.UUID;

public record DocumentResponse(
    UUID id,
    String code,
    String title,
    String description,
    boolean enabled,
    List<Version> versions
) {
  public record Version(UUID id, int version, String note) {}
}


