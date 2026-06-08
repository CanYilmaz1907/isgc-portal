package com.isgc.portal.auditlog;

import com.isgc.portal.common.SecurityUtil;
import com.isgc.portal.security.CurrentUser;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {
  private final AuditLogService auditLogService;

  public AuditLogAspect(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) && execution(* com.isgc.portal..*Controller.*(..))")
  public void postMapping() {}

  @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping) && execution(* com.isgc.portal..*Controller.*(..))")
  public void putMapping() {}

  @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping) && execution(* com.isgc.portal..*Controller.*(..))")
  public void deleteMapping() {}

  @AfterReturning(pointcut = "postMapping()", returning = "result")
  public void logCreate(JoinPoint joinPoint, Object result) {
    try {
      CurrentUser user = SecurityUtil.requireCurrentUser();
      if (user == null) return;

      String methodName = joinPoint.getSignature().getName();
      String controllerName = joinPoint.getTarget().getClass().getSimpleName();
      String action = "CREATE_" + extractEntityType(controllerName);

      UUID entityId = extractEntityId(result);
      Map<String, Object> details = new HashMap<>();
      details.put("method", methodName);
      details.put("controller", controllerName);

      auditLogService.log(action, user, extractEntityType(controllerName), entityId, details);
    } catch (Exception e) {
      // Silently fail to not break the main flow
    }
  }

  @AfterReturning(pointcut = "putMapping()")
  public void logUpdate(JoinPoint joinPoint) {
    try {
      CurrentUser user = SecurityUtil.requireCurrentUser();
      if (user == null) return;

      String methodName = joinPoint.getSignature().getName();
      String controllerName = joinPoint.getTarget().getClass().getSimpleName();
      String action = "UPDATE_" + extractEntityType(controllerName);

      UUID entityId = extractEntityIdFromArgs(joinPoint.getArgs());
      Map<String, Object> details = new HashMap<>();
      details.put("method", methodName);
      details.put("controller", controllerName);

      auditLogService.log(action, user, extractEntityType(controllerName), entityId, details);
    } catch (Exception e) {
      // Silently fail to not break the main flow
    }
  }

  @AfterReturning(pointcut = "deleteMapping()")
  public void logDelete(JoinPoint joinPoint) {
    try {
      CurrentUser user = SecurityUtil.requireCurrentUser();
      if (user == null) return;

      String methodName = joinPoint.getSignature().getName();
      String controllerName = joinPoint.getTarget().getClass().getSimpleName();
      String action = "DELETE_" + extractEntityType(controllerName);

      UUID entityId = extractEntityIdFromArgs(joinPoint.getArgs());
      Map<String, Object> details = new HashMap<>();
      details.put("method", methodName);
      details.put("controller", controllerName);

      auditLogService.log(action, user, extractEntityType(controllerName), entityId, details);
    } catch (Exception e) {
      // Silently fail to not break the main flow
    }
  }

  private String extractEntityType(String controllerName) {
    return controllerName.replace("Controller", "").toUpperCase();
  }

  private UUID extractEntityId(Object result) {
    if (result == null) return null;
    try {
      // Try to extract id from response DTOs
      java.lang.reflect.Method getId = result.getClass().getMethod("id");
      Object id = getId.invoke(result);
      if (id instanceof UUID) return (UUID) id;
    } catch (Exception ignored) {
    }
    return null;
  }

  private UUID extractEntityIdFromArgs(Object[] args) {
    for (Object arg : args) {
      if (arg instanceof UUID) return (UUID) arg;
    }
    return null;
  }
}

