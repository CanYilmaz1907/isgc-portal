package com.isgc.portal.nonconformity;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NonconformityRepository extends JpaRepository<Nonconformity, UUID> {
  @Query("select n from Nonconformity n where n.project.id = :projectId")
  List<Nonconformity> findByProjectId(@Param("projectId") UUID projectId);

  @Query("select n from Nonconformity n where n.status <> com.isgc.portal.nonconformity.NonconformityStatus.CLOSED")
  List<Nonconformity> findOpen();
}


