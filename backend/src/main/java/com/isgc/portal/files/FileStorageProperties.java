package com.isgc.portal.files;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "isgc.files")
public record FileStorageProperties(String storageRoot) {}


