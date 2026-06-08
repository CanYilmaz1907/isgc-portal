package com.isgc.portal.auditlog;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
  Page<AuditLog> findByOrderByCreatedAtDesc(Pageable pageable);

  @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
  List<AuditLog> findByEntity(@Param("entityType") String entityType, @Param("entityId") UUID entityId);

  List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to);

  @Query("SELECT a FROM AuditLog a WHERE a.actorUser.id = :userId ORDER BY a.createdAt DESC")
  List<AuditLog> findByActorUserId(@Param("userId") UUID userId);
}

