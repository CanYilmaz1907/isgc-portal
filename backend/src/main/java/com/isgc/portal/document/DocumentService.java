package com.isgc.portal.document;

import com.isgc.portal.document.dto.DocumentResponse;
import com.isgc.portal.files.FileObject;
import com.isgc.portal.files.FileObjectRepository;
import com.isgc.portal.files.FileStorageService;
import com.isgc.portal.security.CurrentUser;
import com.isgc.portal.user.Role;
import com.isgc.portal.user.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {
  public static final String FILE_MODULE = "DOCUMENT";

  private final DocumentRepository docRepo;
  private final DocumentVersionRepository verRepo;
  private final FileObjectRepository fileRepo;
  private final FileStorageService storage;
  private final UserRepository userRepo;

  public DocumentService(
      DocumentRepository docRepo,
      DocumentVersionRepository verRepo,
      FileObjectRepository fileRepo,
      FileStorageService storage,
      UserRepository userRepo
  ) {
    this.docRepo = docRepo;
    this.verRepo = verRepo;
    this.fileRepo = fileRepo;
    this.storage = storage;
    this.userRepo = userRepo;
  }

  @Transactional(readOnly = true)
  public List<DocumentResponse> list() {
    return docRepo.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public DocumentResponse get(UUID id) {
    return toDto(docRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Document not found")));
  }

  @Transactional
  public DocumentResponse create(CurrentUser user, String code, String title, String description, MultipartFile pdf, String note) {
    if (user.role() != Role.ADMIN) throw new IllegalArgumentException("Access denied");
    if (pdf == null || pdf.isEmpty()) throw new IllegalArgumentException("PDF required");

    Document d = new Document();
    d.setId(UUID.randomUUID());
    d.setCode(code);
    d.setTitle(title);
    d.setDescription(description);
    d.setEnabled(true);
    docRepo.save(d);

    DocumentVersion v = new DocumentVersion();
    v.setId(UUID.randomUUID());
    v.setDocument(d);
    v.setVersion(1);
    v.setNote(note);
    v.setCreatedBy(userRepo.findById(user.id()).orElse(null));
    verRepo.save(v);

    storeFile(user, v.getId(), pdf);
    return toDto(d);
  }

  @Transactional
  public DocumentResponse uploadNewVersion(CurrentUser user, UUID docId, MultipartFile pdf, String note) {
    if (user.role() != Role.ADMIN) throw new IllegalArgumentException("Access denied");
    Document d = docRepo.findById(docId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
    if (pdf == null || pdf.isEmpty()) throw new IllegalArgumentException("PDF required");
    int next = verRepo.findMaxVersion(docId).orElse(0) + 1;

    DocumentVersion v = new DocumentVersion();
    v.setId(UUID.randomUUID());
    v.setDocument(d);
    v.setVersion(next);
    v.setNote(note);
    v.setCreatedBy(userRepo.findById(user.id()).orElse(null));
    verRepo.save(v);
    storeFile(user, v.getId(), pdf);
    return toDto(d);
  }

  @Transactional
  public void delete(CurrentUser user, UUID docId) {
    if (user.role() != Role.ADMIN) throw new IllegalArgumentException("Access denied");
    docRepo.deleteById(docId);
  }

  @Transactional(readOnly = true)
  public FileObject requireVersionFile(UUID versionId) {
    var files = fileRepo.findByModuleAndEntityId(FILE_MODULE, versionId);
    if (files.isEmpty()) throw new IllegalArgumentException("File not found");
    return files.get(0);
  }

  private void storeFile(CurrentUser user, UUID versionId, MultipartFile file) {
    if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
      throw new IllegalArgumentException("Filename required");
    }
    UUID fileId = UUID.randomUUID();
    var stored = storage.store(FILE_MODULE, versionId, fileId, safeInputStream(file));
    FileObject fo = new FileObject();
    fo.setId(fileId);
    fo.setModule(FILE_MODULE);
    fo.setEntityId(versionId);
    fo.setOriginalFilename(file.getOriginalFilename());
    fo.setContentType(file.getContentType());
    fo.setSizeBytes(stored.sizeBytes());
    fo.setStoragePath(stored.storagePath());
    fo.setSha256(stored.sha256());
    fo.setUploadedBy(userRepo.findById(user.id()).orElse(null));
    fileRepo.save(fo);
  }

  private static java.io.InputStream safeInputStream(MultipartFile file) {
    try {
      return file.getInputStream();
    } catch (Exception e) {
      throw new IllegalStateException("Cannot read upload", e);
    }
  }

  private DocumentResponse toDto(Document d) {
    var versions = verRepo.findByDocumentId(d.getId()).stream()
        .map(v -> new DocumentResponse.Version(v.getId(), v.getVersion(), v.getNote()))
        .toList();
    return new DocumentResponse(d.getId(), d.getCode(), d.getTitle(), d.getDescription(), d.isEnabled(), versions);
  }
}


