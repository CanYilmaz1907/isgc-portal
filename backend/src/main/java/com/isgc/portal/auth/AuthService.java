package com.isgc.portal.auth;

import com.isgc.portal.auth.dto.TokenResponse;
import com.isgc.portal.security.JwtService;
import com.isgc.portal.security.SecurityProperties;
import com.isgc.portal.security.TokenHashingService;
import com.isgc.portal.user.User;
import com.isgc.portal.user.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private static final SecureRandom RNG = new SecureRandom();
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final TokenHashingService tokenHashingService;
  private final SecurityProperties props;

  public AuthService(
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      TokenHashingService tokenHashingService,
      SecurityProperties props
  ) {
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.tokenHashingService = tokenHashingService;
    this.props = props;
  }

  @Transactional
  public TokenResponse login(String username, String password) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    if (!user.isEnabled()) {
      throw new IllegalArgumentException("User disabled");
    }
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }
    return issueTokens(user);
  }

  @Transactional
  public TokenResponse refresh(String rawRefreshToken) {
    String hash = tokenHashingService.sha256Base64Url(rawRefreshToken);
    RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
        .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

    if (token.isRevoked()) {
      throw new IllegalArgumentException("Refresh token revoked");
    }
    if (token.getExpiresAt().isBefore(Instant.now())) {
      throw new IllegalArgumentException("Refresh token expired");
    }

    // rotation
    token.setRevoked(true);
    refreshTokenRepository.save(token);

    return issueTokens(token.getUser());
  }

  @Transactional
  public void logout(String rawRefreshToken) {
    String hash = tokenHashingService.sha256Base64Url(rawRefreshToken);
    refreshTokenRepository.findByTokenHash(hash).ifPresent(t -> {
      t.setRevoked(true);
      refreshTokenRepository.save(t);
    });
  }

  @Transactional
  public int cleanupExpiredTokens() {
    return refreshTokenRepository.deleteExpiredOrRevoked(Instant.now());
  }

  private TokenResponse issueTokens(User user) {
    String access = jwtService.issueAccessToken(user.getId(), user.getUsername(), user.getRole());
    Instant accessExp = Instant.now().plus(Duration.ofMinutes(props.accessTokenMinutes()));
    String refreshRaw = generateRefreshToken();

    RefreshToken rt = new RefreshToken();
    rt.setId(UUID.randomUUID());
    rt.setUser(user);
    rt.setTokenHash(tokenHashingService.sha256Base64Url(refreshRaw));
    rt.setExpiresAt(Instant.now().plus(Duration.ofDays(props.refreshTokenDays())));
    rt.setRevoked(false);
    refreshTokenRepository.save(rt);

    return new TokenResponse(access, refreshRaw, accessExp, user.getId(), user.getUsername(), user.getRole());
  }

  private static String generateRefreshToken() {
    byte[] buf = new byte[64];
    RNG.nextBytes(buf);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
  }
}


