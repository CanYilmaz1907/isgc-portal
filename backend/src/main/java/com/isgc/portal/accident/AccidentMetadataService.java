package com.isgc.portal.accident;

import com.isgc.portal.accident.dto.AccidentMetadataResponse;
import com.isgc.portal.accident.dto.CauseSelectionDto;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccidentMetadataService {
  private final CauseCategoryRepository causeCategoryRepo;
  private final AccidentLookupOptionRepository lookupRepo;

  public AccidentMetadataService(CauseCategoryRepository causeCategoryRepo, AccidentLookupOptionRepository lookupRepo) {
    this.causeCategoryRepo = causeCategoryRepo;
    this.lookupRepo = lookupRepo;
  }

  @Transactional(readOnly = true)
  public AccidentMetadataResponse metadata() {
    return new AccidentMetadataResponse(
        toLookup("classification"),
        toLookup("area"),
        toLookup("timeRange"),
        toLookup("hazardSource"),
        toLookup("injuryType"),
        toLookup("injuredBodyPart"),
        toCauseGroups("DIRECT"),
        toCauseGroups("ROOT")
    );
  }

  private List<AccidentMetadataResponse.LookupOption> toLookup(String type) {
    return lookupRepo.findByOptionTypeAndEnabledTrueOrderBySortOrderAsc(type).stream()
        .map(o -> new AccidentMetadataResponse.LookupOption(o.getOptionCode(), o.getOptionLabel()))
        .toList();
  }

  private List<AccidentMetadataResponse.CauseGroup> toCauseGroups(String causeType) {
    Map<String, List<CauseSelectionDto>> itemsByKey = new LinkedHashMap<>();
    Map<String, String[]> metaByKey = new LinkedHashMap<>();
    for (CauseCategory c : causeCategoryRepo.findByCauseTypeAndEnabledTrueOrderBySortOrderAsc(causeType)) {
      String key = c.getGroupCode() + "|" + c.getSection();
      metaByKey.putIfAbsent(key, new String[] { c.getSection(), c.getGroupCode(), c.getGroupName() });
      itemsByKey.computeIfAbsent(key, k -> new ArrayList<>())
          .add(new CauseSelectionDto(c.getItemCode(), c.getItemLabel()));
    }
    List<AccidentMetadataResponse.CauseGroup> out = new ArrayList<>();
    for (var entry : itemsByKey.entrySet()) {
      String[] meta = metaByKey.get(entry.getKey());
      out.add(new AccidentMetadataResponse.CauseGroup(meta[0], meta[1], meta[2], entry.getValue()));
    }
    return out;
  }
}
