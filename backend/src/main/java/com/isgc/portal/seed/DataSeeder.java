package com.isgc.portal.seed;

import com.isgc.portal.user.Role;
import com.isgc.portal.user.User;
import com.isgc.portal.user.UserRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    log.info("DataSeeder: Starting user seeding...");
    seedUser("irina", "irina@isgc.local", "irina123", Role.ADMIN);
    seedUser("samet", "samet@isgc.local", "samet123", Role.ADMIN);
    seedUser("readonly", "readonly@isgc.local", "readonly123", Role.READ_ONLY);
    log.info("DataSeeder: User seeding completed.");
  }

  private void seedUser(String username, String email, String password, Role role) {
    User existingUser = userRepository.findByUsername(username).orElse(null);
    if (existingUser != null) {
      log.info("DataSeeder: User '{}' already exists, updating...", username);
      existingUser.setPasswordHash(passwordEncoder.encode(password));
      existingUser.setEmail(email);
      existingUser.setRole(role);
      existingUser.setEnabled(true);
      userRepository.save(existingUser);
      log.info("DataSeeder: User '{}' updated.", username);
      return;
    }
    log.info("DataSeeder: Creating user '{}' ({})...", username, role);
    User u = new User();
    u.setId(UUID.randomUUID());
    u.setUsername(username);
    u.setEmail(email);
    u.setRole(role);
    u.setPasswordHash(passwordEncoder.encode(password));
    u.setEnabled(true);
    userRepository.save(u);
    log.info("DataSeeder: User '{}' created.", username);
  }
}
