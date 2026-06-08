package com.isgc.portal.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditParticipantRepository extends JpaRepository<AuditParticipant, UUID> {
  @Query("select p from AuditParticipant p where p.audit.id = :auditId")
  List<AuditParticipant> findByAuditId(@Param("auditId") UUID auditId);

  @Modifying
  @Query("delete from AuditParticipant p where p.audit.id = :auditId")
  int deleteByAuditId(@Param("auditId") UUID auditId);
}


