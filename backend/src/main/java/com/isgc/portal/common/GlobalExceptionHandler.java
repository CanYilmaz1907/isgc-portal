package com.isgc.portal.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, Object> details = new HashMap<>();
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fe.getField(), fe.getDefaultMessage());
    }
    details.put("fieldErrors", fieldErrors);
    ApiError err = ApiError.of(
        HttpStatus.BAD_REQUEST.value(),
        "ValidationError",
        "Validation failed",
        req.getRequestURI(),
        details
    );
    return ResponseEntity.badRequest().body(err);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
    String msg = ex.getMessage() != null ? ex.getMessage() : "Bad request";
    int status = HttpStatus.BAD_REQUEST.value();
    if (msg.contains("not found") || msg.contains("Not found")) {
      status = HttpStatus.NOT_FOUND.value();
    } else if (msg.contains("Access denied") || msg.contains("access denied")) {
      status = HttpStatus.FORBIDDEN.value();
    }
    ApiError err = ApiError.of(status, status == 404 ? "NotFound" : status == 403 ? "Forbidden" : "BadRequest", msg, req.getRequestURI(), Map.of());
    return ResponseEntity.status(status).body(err);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
    org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class).error("Unexpected error", ex);
    ApiError err = ApiError.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "InternalServerError",
        ex.getMessage() != null ? ex.getMessage() : "Unexpected error",
        req.getRequestURI(),
        Map.of()
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
  }
}


