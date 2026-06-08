package com.isgc.portal.accident;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccidentPersonRepository extends JpaRepository<AccidentPerson, UUID> {
  @Query("select ap from AccidentPerson ap where ap.accident.id = :accidentId")
  List<AccidentPerson> findByAccidentId(@Param("accidentId") UUID accidentId);

  @Query("select ap from AccidentPerson ap left join fetch ap.employee where ap.accident.id = :accidentId")
  List<AccidentPerson> findByAccidentIdWithEmployee(@Param("accidentId") UUID accidentId);

  @Modifying
  @Query("delete from AccidentPerson ap where ap.accident.id = :accidentId")
  int deleteByAccidentId(@Param("accidentId") UUID accidentId);
}


