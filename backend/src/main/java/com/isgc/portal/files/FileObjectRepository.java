package com.isgc.portal.files;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileObjectRepository extends JpaRepository<FileObject, UUID> {
  @Query("select f from FileObject f where f.module = :module and f.entityId = :entityId")
  List<FileObject> findByModuleAndEntityId(@Param("module") String module, @Param("entityId") UUID entityId);
}


