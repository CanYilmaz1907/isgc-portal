package com.isgc.portal.accident.report;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccidentReportSubscriptionRepository extends JpaRepository<AccidentReportSubscription, UUID> {
  @Query("select s from AccidentReportSubscription s where s.enabled = true")
  List<AccidentReportSubscription> findEnabled();

  @Query("select s from AccidentReportSubscription s where s.project.id = :projectId")
  List<AccidentReportSubscription> findByProjectId(@Param("projectId") UUID projectId);
}


