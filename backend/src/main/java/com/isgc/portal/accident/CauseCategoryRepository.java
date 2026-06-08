package com.isgc.portal.accident;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CauseCategoryRepository extends JpaRepository<CauseCategory, UUID> {
  List<CauseCategory> findByCauseTypeAndEnabledTrueOrderBySortOrderAsc(String causeType);

  long countByCauseType(String causeType);
}
