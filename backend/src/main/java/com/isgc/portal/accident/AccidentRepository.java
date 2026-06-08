package com.isgc.portal.accident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccidentRepository extends JpaRepository<Accident, UUID> {
  @Query("select a from Accident a where a.project.id = :projectId")
  List<Accident> findByProjectId(@Param("projectId") UUID projectId);

  @Query("select a from Accident a left join fetch a.accidentType left join fetch a.supervisorEmployee where a.id = :id")
  Optional<Accident> findByIdWithRelations(@Param("id") UUID id);

  @Query(value = "SELECT nextval('accidents_incident_no_seq')", nativeQuery = true)
  Long nextIncidentNo();
}


