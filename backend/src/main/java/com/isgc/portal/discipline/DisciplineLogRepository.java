package com.isgc.portal.discipline;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DisciplineLogRepository extends JpaRepository<DisciplineLog, UUID> {
  @Query("select d from DisciplineLog d where d.project.id = :projectId")
  List<DisciplineLog> findByProjectId(@Param("projectId") UUID projectId);

  @Query("select d from DisciplineLog d left join fetch d.violatingEmployee left join fetch d.violatingManagerEmployee where d.id = :id")
  Optional<DisciplineLog> findByIdWithEmployees(@Param("id") UUID id);

  @Query(value = "SELECT nextval('discipline_logs_seq')", nativeQuery = true)
  Long nextSequenceNo();

  long countByEmployeeRegistrationNoAndOccurredAtAfter(String employeeRegistrationNo, Instant occurredAt);

  List<DisciplineLog> findByEmployeeRegistrationNoAndOccurredAtAfterOrderByOccurredAtAsc(
      String employeeRegistrationNo,
      Instant occurredAt
  );
}
