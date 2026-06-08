package com.isgc.portal.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "isgc.security.jwt")
public record SecurityProperties(
    String issuer,
    long accessTokenMinutes,
    long refreshTokenDays,
    String secret
) {}


