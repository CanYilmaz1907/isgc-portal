package com.isgc.portal.discipline.dto;

import java.util.List;
import java.util.Map;

public record DisciplineMetadataResponse(
    List<Option> categories,
    Map<String, List<Option>> violationTypes,
    List<Option> statuses
) {
  public record Option(String value, String label) {}
}
