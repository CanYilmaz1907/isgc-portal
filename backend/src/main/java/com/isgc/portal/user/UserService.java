package com.isgc.portal.user;

import com.isgc.portal.user.dto.UserRequest;
import com.isgc.portal.user.dto.UserResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<UserResponse> findAll() {
    return userRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  public UserResponse findById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return toResponse(user);
  }

  @Transactional
  public UserResponse create(UserRequest req) {
    if (userRepository.findByUsername(req.username()).isPresent()) {
      throw new IllegalArgumentException("Username already exists");
    }
    if (userRepository.findByEmail(req.email()).isPresent()) {
      throw new IllegalArgumentException("Email already exists");
    }

    User user = new User();
    user.setId(UUID.randomUUID());
    user.setUsername(req.username());
    user.setEmail(req.email());
    user.setPasswordHash(passwordEncoder.encode(req.password()));
    user.setRole(req.role());
    user.setEnabled(req.isEnabled());
    
    userRepository.save(user);
    return toResponse(user);
  }

  @Transactional
  public UserResponse update(UUID id, UserRequest req) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Check username uniqueness (if changed)
    if (!user.getUsername().equals(req.username())) {
      if (userRepository.findByUsername(req.username()).isPresent()) {
        throw new IllegalArgumentException("Username already exists");
      }
      user.setUsername(req.username());
    }

    // Check email uniqueness (if changed)
    if (!user.getEmail().equals(req.email())) {
      if (userRepository.findByEmail(req.email()).isPresent()) {
        throw new IllegalArgumentException("Email already exists");
      }
      user.setEmail(req.email());
    }

    // Update password only if provided
    if (req.password() != null && !req.password().isEmpty()) {
      user.setPasswordHash(passwordEncoder.encode(req.password()));
    }

    user.setRole(req.role());
    user.setEnabled(req.isEnabled());
    
    userRepository.save(user);
    return toResponse(user);
  }

  @Transactional
  public void delete(UUID id) {
    if (!userRepository.existsById(id)) {
      throw new IllegalArgumentException("User not found");
    }
    userRepository.deleteById(id);
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole(),
        user.isEnabled(),
        user.getCreatedAt(),
        user.getUpdatedAt()
    );
  }
}

