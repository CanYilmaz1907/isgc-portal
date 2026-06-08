package com.isgc.portal.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, UUID> {
  @Query("select i from ChecklistItem i where i.checklist.id = :checklistId order by i.itemNo asc")
  List<ChecklistItem> findByChecklistId(@Param("checklistId") UUID checklistId);
}


