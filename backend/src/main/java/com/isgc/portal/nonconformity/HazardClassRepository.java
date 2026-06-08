package com.isgc.portal.nonconformity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HazardClassRepository extends JpaRepository<HazardClass, UUID> {
  Optional<HazardClass> findByCode(String code);

  List<HazardClass> findByEnabledTrue();
}


