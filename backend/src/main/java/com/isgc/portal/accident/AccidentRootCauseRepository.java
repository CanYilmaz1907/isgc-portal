package com.isgc.portal.accident;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccidentRootCauseRepository extends JpaRepository<AccidentRootCause, UUID> {
  List<AccidentRootCause> findByAccidentId(UUID accidentId);

  void deleteByAccidentId(UUID accidentId);
}
