package com.isgc.portal.training;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, UUID> {
  @Query("select t from TrainingRecord t where t.employee.id = :employeeId")
  List<TrainingRecord> findByEmployeeId(@Param("employeeId") UUID employeeId);
}


