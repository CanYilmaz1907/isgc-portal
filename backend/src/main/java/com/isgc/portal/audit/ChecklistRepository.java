package com.isgc.portal.audit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistRepository extends JpaRepository<Checklist, UUID> {
  Optional<Checklist> findByCode(String code);

  List<Checklist> findByEnabledTrue();
}


