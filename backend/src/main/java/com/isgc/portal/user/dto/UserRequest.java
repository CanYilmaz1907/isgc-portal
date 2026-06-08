package com.isgc.portal.user.dto;

import com.isgc.portal.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank
    @Size(min = 3, max = 80)
    String username,
    
    @NotBlank
    @Email
    String email,
    
    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,
    
    @NotNull
    Role role,
    
    Boolean enabled
) {
  public boolean isEnabled() {
    return enabled != null ? enabled : true;
  }
}

