package com.isgc.portal.accident.dto;

import java.util.List;

public record AccidentMetadataResponse(
    List<LookupOption> classifications,
    List<LookupOption> areas,
    List<LookupOption> timeRanges,
    List<LookupOption> hazardSources,
    List<LookupOption> injuryTypes,
    List<LookupOption> injuredBodyParts,
    List<CauseGroup> directCauseGroups,
    List<CauseGroup> rootCauseGroups
) {
  public record LookupOption(String code, String label) {}

  public record CauseGroup(String section, String groupCode, String groupName, List<CauseSelectionDto> items) {}
}
