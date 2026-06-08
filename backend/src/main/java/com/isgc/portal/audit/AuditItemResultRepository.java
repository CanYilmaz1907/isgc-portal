package com.isgc.portal.audit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditItemResultRepository extends JpaRepository<AuditItemResult, UUID> {
  @Query("select r from AuditItemResult r where r.audit.id = :auditId")
  List<AuditItemResult> findByAuditId(@Param("auditId") UUID auditId);

  @Query("select r from AuditItemResult r where r.audit.id = :auditId and r.checklistItem.id = :itemId")
  Optional<AuditItemResult> findByAuditIdAndItemId(@Param("auditId") UUID auditId, @Param("itemId") UUID itemId);
}


