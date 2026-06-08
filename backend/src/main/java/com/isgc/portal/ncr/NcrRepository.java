package com.isgc.portal.ncr;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NcrRepository extends JpaRepository<Ncr, UUID> {
  @Query("select n from Ncr n where n.project.id = :projectId")
  List<Ncr> findByProjectId(@Param("projectId") UUID projectId);

  @Query("select count(n) from Ncr n where n.ncrDate = :ncrDate")
  long countByNcrDate(@Param("ncrDate") LocalDate ncrDate);

  @Query("select count(n) from Ncr n where n.ncrDate >= :start and n.ncrDate < :end")
  long countByNcrDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
