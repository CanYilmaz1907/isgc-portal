package com.isgc.portal.seed;

import com.isgc.portal.employee.Employee;
import com.isgc.portal.employee.EmployeeRepository;
import com.isgc.portal.user.UserRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Temporary endpoint to manually create sample employees.
 * This can be removed after initial setup.
 */
@RestController
@RequestMapping("/api/admin/setup")
public class EmployeeSeedController {
  private static final Logger log = LoggerFactory.getLogger(EmployeeSeedController.class);
  private final EmployeeRepository employeeRepository;
  private final UserRepository userRepository;

  public EmployeeSeedController(EmployeeRepository employeeRepository, UserRepository userRepository) {
    this.employeeRepository = employeeRepository;
    this.userRepository = userRepository;
  }

  @PostMapping("/cleanup-random-employees")
  @Transactional
  public ResponseEntity<String> cleanupRandomEmployees() {
    try {
      int deleted = 0;
      var allEmployees = employeeRepository.findAll();
      var toDelete = new java.util.ArrayList<Employee>();
      
      // List of random names to delete
      var randomNames = java.util.Set.of("Ahmet", "Mehmet", "Ayşe", "Fatma", "Ali", "Zeynep", "Mustafa", "Elif");
      
      for (var emp : allEmployees) {
        boolean shouldDelete = false;
        String username = null;
        
        try {
          // Check user link (need to access within transaction)
          if (emp.getUser() == null) {
            shouldDelete = true;
            log.info("Found employee without user: {} {}", emp.getFirstName(), emp.getLastName());
          } else {
            username = emp.getUser().getUsername();
            // Delete if matches random names (likely from old seed)
            if (randomNames.contains(emp.getFirstName())) {
              shouldDelete = true;
              log.info("Found random employee by name: {} {} (User: {})", 
                  emp.getFirstName(), emp.getLastName(), username);
            }
          }
        } catch (Exception ex) {
          log.warn("Error checking employee {} {}: {}", emp.getFirstName(), emp.getLastName(), ex.getMessage());
          // If we can't check user, and name matches random, delete it
          if (randomNames.contains(emp.getFirstName())) {
            shouldDelete = true;
          }
        }
        
        if (shouldDelete) {
          toDelete.add(emp);
        }
      }
      
      for (var emp : toDelete) {
        try {
          employeeRepository.delete(emp);
          deleted++;
          log.info("Deleted: {} {}", emp.getFirstName(), emp.getLastName());
        } catch (Exception ex) {
          log.warn("Failed to delete employee {} {}: {}", emp.getFirstName(), emp.getLastName(), ex.getMessage());
        }
      }
      
      return ResponseEntity.ok(String.format("Silinen random çalışan: %d (Toplam kontrol edilen: %d)", deleted, allEmployees.size()));
    } catch (Exception e) {
      log.error("Error cleaning up random employees", e);
      return ResponseEntity.internalServerError().body("Hata: " + e.getMessage());
    }
  }

  @PostMapping("/create-sample-employees")
  public ResponseEntity<String> createSampleEmployees() {
    try {
      int created = 0;
      int skipped = 0;
      int deleted = 0;

      // First, delete ALL employees that are NOT linked to any user (random employees)
      var allEmployees = employeeRepository.findAll();
      for (var emp : allEmployees) {
        if (emp.getUser() == null) {
          log.info("Deleting random employee without user: {} {} (ID: {})", emp.getFirstName(), emp.getLastName(), emp.getId());
          try {
            employeeRepository.delete(emp);
            deleted++;
          } catch (Exception ex) {
            log.warn("Failed to delete employee {} {}: {}", emp.getFirstName(), emp.getLastName(), ex.getMessage());
          }
        }
      }
      
      if (deleted > 0) {
        log.info("Deleted {} random employees without user links", deleted);
      }

      // Get all users from database
      var allUsers = userRepository.findAll();
      
      if (allUsers.isEmpty()) {
        return ResponseEntity.badRequest().body("Veritabanında kullanıcı bulunamadı. Önce kullanıcı oluşturun.");
      }

      // Create employee records for each user
      for (var user : allUsers) {
        // Check if user already has an employee record
        var existingEmployee = employeeRepository.findByUserId(user.getId());
        
        if (existingEmployee.isPresent()) {
          log.info("User '{}' already has employee record, skipping...", user.getUsername());
          skipped++;
          continue;
        }

        // Determine job title and profession based on role
        String jobTitle = getJobTitleForRole(user.getRole());
        String profession = getProfessionForRole(user.getRole());
        String employeeNo = "EMP" + user.getUsername().toUpperCase();

        // Create employee record for this user
        Employee e = new Employee();
        e.setId(UUID.randomUUID());
        e.setEmployeeNo(employeeNo);
        e.setFirstName(extractFirstName(user.getUsername(), user.getEmail()));
        e.setLastName(extractLastName(user.getUsername(), user.getEmail()));
        e.setJobTitle(jobTitle);
        e.setProfession(profession);
        e.setUser(user);
        e.setEnabled(true);

        try {
          employeeRepository.save(e);
          log.info("Employee created for user '{}': {} {}", user.getUsername(), e.getFirstName(), e.getLastName());
          created++;
        } catch (Exception ex) {
          log.warn("Failed to create employee for user '{}': {}", user.getUsername(), ex.getMessage());
          skipped++;
        }
      }

      return ResponseEntity.ok(String.format("Çalışan kayıtları güncellendi. Silinen random: %d, Yeni: %d, Zaten var: %d (Toplam kullanıcı: %d)", 
          deleted, created, skipped, allUsers.size()));
    } catch (Exception e) {
      log.error("Error creating employees from users", e);
      return ResponseEntity.internalServerError().body("Hata: " + e.getMessage());
    }
  }

  private String getJobTitleForRole(com.isgc.portal.user.Role role) {
    return switch (role) {
      case ADMIN -> "Sistem Yöneticisi";
      case ISG_C -> "İSG Uzmanı";
      case YONETICI -> "Proje Yöneticisi";
      case PERSONEL -> "Personel";
      case READ_ONLY -> "Salt Okunur Görüntüleyici";
    };
  }

  private String getProfessionForRole(com.isgc.portal.user.Role role) {
    return switch (role) {
      case ADMIN -> "Yönetici";
      case ISG_C -> "İSG";
      case YONETICI -> "Yönetici";
      case PERSONEL -> "Personel";
      case READ_ONLY -> "Görüntüleyici";
    };
  }

  private String extractFirstName(String username, String email) {
    // Try to extract from email first
    if (email != null && email.contains("@")) {
      String localPart = email.split("@")[0];
      if (localPart.contains(".")) {
        return capitalize(localPart.split("\\.")[0]);
      }
    }
    // Fallback to username
    return capitalize(username);
  }

  private String extractLastName(String username, String email) {
    // Try to extract from email first
    if (email != null && email.contains("@")) {
      String localPart = email.split("@")[0];
      if (localPart.contains(".")) {
        String[] parts = localPart.split("\\.");
        if (parts.length > 1) {
          return capitalize(parts[1]);
        }
      }
    }
    // Fallback: use username with a default last name
    return "User";
  }

  private String capitalize(String str) {
    if (str == null || str.isEmpty()) return str;
    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }

}

