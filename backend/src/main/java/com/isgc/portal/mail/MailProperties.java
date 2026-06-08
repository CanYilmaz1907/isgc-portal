package com.isgc.portal.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "isgc.mail")
public record MailProperties(String from, String managementCc) {}


