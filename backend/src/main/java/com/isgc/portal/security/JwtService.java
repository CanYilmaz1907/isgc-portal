package com.isgc.portal.security;

import com.isgc.portal.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecurityProperties props;
  private final SecretKey key;

  /** Minimum 32 bytes (256 bits) for HS256. */
  private static final String FALLBACK_SECRET = "change-me-in-prod-change-me-in-prod-change-me-in-prod";

  public JwtService(SecurityProperties props) {
    this.props = props;
    String raw = props.secret() != null && !props.secret().isBlank() ? props.secret() : FALLBACK_SECRET;
    this.key = Keys.hmacShaKeyFor(raw.getBytes(StandardCharsets.UTF_8));
  }

  public String issueAccessToken(UUID userId, String username, Role role) {
    Instant now = Instant.now();
    Instant exp = now.plus(Duration.ofMinutes(props.accessTokenMinutes()));

    return Jwts.builder()
        .issuer(props.issuer())
        .subject(userId.toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claims(Map.of(
            "username", username,
            "role", role.name()
        ))
        .signWith(key)
        .compact();
  }

  public Claims parseAndValidate(String jwt) {
    return Jwts.parser()
        .verifyWith(key)
        .requireIssuer(props.issuer())
        .build()
        .parseSignedClaims(jwt)
        .getPayload();
  }
}


