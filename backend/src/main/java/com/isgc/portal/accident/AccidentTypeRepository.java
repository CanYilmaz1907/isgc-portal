package com.isgc.portal.accident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccidentTypeRepository extends JpaRepository<AccidentType, UUID> {
  Optional<AccidentType> findByCode(String code);

  List<AccidentType> findByEnabledTrue();
}


