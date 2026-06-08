package com.isgc.portal.auditlog;

import com.isgc.portal.auditlog.dto.AuditLogResponse;
import com.isgc.portal.security.CurrentUser;
import com.isgc.portal.user.User;
import com.isgc.portal.user.UserRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
  private final AuditLogRepository auditLogRepository;
  private final UserRepository userRepository;

  public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
    this.auditLogRepository = auditLogRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public void log(String action, CurrentUser actor, String entityType, UUID entityId, Map<String, Object> details) {
    AuditLog log = new AuditLog();
    log.setId(UUID.randomUUID());
    if (actor != null) {
      User user = userRepository.findById(actor.id()).orElse(null);
      log.setActorUser(user);
      log.setActorUsername(actor.username());
    }
    log.setAction(action);
    log.setEntityType(entityType);
    log.setEntityId(entityId);
    if (details != null && !details.isEmpty()) {
      log.setDetails(details);
    }
    log.setCreatedAt(Instant.now());
    auditLogRepository.save(log);
  }

  @Transactional
  public void log(String action, CurrentUser actor, String entityType, UUID entityId) {
    log(action, actor, entityType, entityId, null);
  }

  @Transactional
  public void log(String action, CurrentUser actor) {
    log(action, actor, null, null, null);
  }

  public Page<AuditLogResponse> list(Pageable pageable) {
    Page<AuditLog> logs = auditLogRepository.findByOrderByCreatedAtDesc(pageable);
    return logs.map(this::toDto);
  }

  public List<AuditLogResponse> findByEntity(String entityType, UUID entityId) {
    return auditLogRepository.findByEntity(entityType, entityId).stream().map(this::toDto).toList();
  }

  public List<AuditLogResponse> findByDateRange(Instant from, Instant to) {
    return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to).stream().map(this::toDto).toList();
  }

  public List<AuditLogResponse> findByActor(UUID userId) {
    return auditLogRepository.findByActorUserId(userId).stream().map(this::toDto).toList();
  }

  @SuppressWarnings("unchecked")
  private AuditLogResponse toDto(AuditLog log) {
    return new AuditLogResponse(
        log.getId(),
        log.getActorUser() != null ? log.getActorUser().getId() : null,
        log.getActorUsername(),
        log.getAction(),
        log.getEntityType(),
        log.getEntityId(),
        log.getDetails() != null ? (Map<String, Object>) log.getDetails() : new HashMap<>(),
        log.getCreatedAt()
    );
  }
}

