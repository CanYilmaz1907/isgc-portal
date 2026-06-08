package com.isgc.portal.document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
  @Query("select v from DocumentVersion v where v.document.id = :documentId order by v.version desc")
  List<DocumentVersion> findByDocumentId(@Param("documentId") UUID documentId);

  @Query("select max(v.version) from DocumentVersion v where v.document.id = :documentId")
  Optional<Integer> findMaxVersion(@Param("documentId") UUID documentId);
}


