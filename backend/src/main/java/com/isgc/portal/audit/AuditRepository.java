package com.isgc.portal.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditRepository extends JpaRepository<Audit, UUID> {
  @Query("select a from Audit a where a.project.id = :projectId")
  List<Audit> findByProjectId(@Param("projectId") UUID projectId);
}


