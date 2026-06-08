package com.isgc.portal.security;

import com.isgc.portal.user.Role;
import java.util.UUID;

public record CurrentUser(UUID id, String username, Role role) {}


