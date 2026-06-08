package com.isgc.portal.files;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FileStorageService {
  private final FileStorageProperties props;

  public FileStorageService(FileStorageProperties props) {
    this.props = props;
  }

  public StoredFile store(String module, UUID entityId, UUID fileId, InputStream in) {
    try {
      Path root = Path.of(props.storageRoot()).toAbsolutePath().normalize();
      Path dir = root.resolve(module).resolve(entityId.toString()).normalize();
      if (!dir.startsWith(root)) {
        throw new IllegalArgumentException("Invalid storage path");
      }
      Files.createDirectories(dir);

      Path target = dir.resolve(fileId.toString()).normalize();
      if (!target.startsWith(root)) {
        throw new IllegalArgumentException("Invalid storage path");
      }

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      long size;
      try (var dis = new java.security.DigestInputStream(in, digest)) {
        size = Files.copy(dis, target);
      }
      String sha256 = HexFormat.of().formatHex(digest.digest());
      return new StoredFile(target.toString(), size, sha256);
    } catch (Exception e) {
      throw new IllegalStateException("File store failed", e);
    }
  }

  public record StoredFile(String storagePath, long sizeBytes, String sha256) {}
}


