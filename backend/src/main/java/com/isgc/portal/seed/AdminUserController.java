package com.isgc.portal.seed;

import com.isgc.portal.accident.AccidentImportService;
import com.isgc.portal.accident.dto.AccidentImportResult;
import com.isgc.portal.user.Role;
import com.isgc.portal.user.User;
import com.isgc.portal.user.UserRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Temporary endpoint to manually create/update seed users.
 * This can be removed after initial setup.
 */
@RestController
@RequestMapping("/api/admin/setup")
public class AdminUserController {
  private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AccidentImportService accidentImportService;

  public AdminUserController(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AccidentImportService accidentImportService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.accidentImportService = accidentImportService;
  }

  @PostMapping("/create-users")
  public ResponseEntity<String> createSeedUsers() {
    try {
      createOrUpdateUser("irina", "irina@isgc.local", "irina123", Role.ADMIN);
      createOrUpdateUser("samet", "samet@isgc.local", "samet123", Role.ADMIN);
      createOrUpdateUser("readonly", "readonly@isgc.local", "readonly123", Role.READ_ONLY);
      return ResponseEntity.ok(
          "Seed users created/updated. Admin: irina/irina123, samet/samet123. Read-only: readonly/readonly123");
    } catch (Exception e) {
      log.error("Error creating seed users", e);
      return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
    }
  }

  @PostMapping("/import-accidents")
  public ResponseEntity<AccidentImportResult> importAccidents(
      @RequestParam(name = "projectCode", defaultValue = "KULTUMA") String projectCode,
      @RequestParam(name = "replaceExisting", defaultValue = "true") boolean replaceExisting,
      @RequestParam("file") MultipartFile file
  ) {
    try {
      AccidentImportResult result = accidentImportService.importExcel(projectCode, replaceExisting, file);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Accident import failed", e);
      return ResponseEntity.internalServerError()
          .body(new AccidentImportResult(0, 0, List.of("Import failed: " + e.getMessage())));
    }
  }

  private void createOrUpdateUser(String username, String email, String password, Role role) {
    User existingUser = userRepository.findByUsername(username).orElse(null);
    if (existingUser != null) {
      log.info("Updating user '{}'", username);
      existingUser.setPasswordHash(passwordEncoder.encode(password));
      existingUser.setEmail(email);
      existingUser.setRole(role);
      existingUser.setEnabled(true);
      userRepository.save(existingUser);
      log.info("User '{}' updated successfully", username);
    } else {
      log.info("Creating new user '{}'", username);
      User u = new User();
      u.setId(UUID.randomUUID());
      u.setUsername(username);
      u.setEmail(email);
      u.setRole(role);
      u.setPasswordHash(passwordEncoder.encode(password));
      u.setEnabled(true);
      userRepository.save(u);
      log.info("User '{}' created successfully", username);
    }
  }
}
