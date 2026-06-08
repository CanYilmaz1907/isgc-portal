package com.isgc.portal.accident;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccidentDirectCauseRepository extends JpaRepository<AccidentDirectCause, UUID> {
  List<AccidentDirectCause> findByAccidentId(UUID accidentId);

  void deleteByAccidentId(UUID accidentId);
}
