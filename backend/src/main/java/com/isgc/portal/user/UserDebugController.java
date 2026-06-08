package com.isgc.portal.user;

import com.isgc.portal.security.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Debug endpoint to check current user and role
 */
@RestController
@RequestMapping("/api/debug")
public class UserDebugController {
  
  @GetMapping("/me")
  @PreAuthorize("authenticated")
  public ResponseEntity<?> me(@AuthenticationPrincipal CurrentUser user) {
    if (user == null) {
      return ResponseEntity.ok("No authenticated user");
    }
    return ResponseEntity.ok(new UserInfo(
        user.id().toString(),
        user.username(),
        user.role().name(),
        "ROLE_" + user.role().name()
    ));
  }

  @GetMapping("/admin-test")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> adminTest() {
    return ResponseEntity.ok("ADMIN access granted");
  }

  record UserInfo(String id, String username, String role, String authority) {}
}

