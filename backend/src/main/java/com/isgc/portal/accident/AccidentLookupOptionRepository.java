package com.isgc.portal.accident;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccidentLookupOptionRepository extends JpaRepository<AccidentLookupOption, UUID> {
  List<AccidentLookupOption> findByOptionTypeAndEnabledTrueOrderBySortOrderAsc(String optionType);

  long count();
}
