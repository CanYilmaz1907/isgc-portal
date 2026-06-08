package com.isgc.portal.nonconformity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NonconformityTemplateRepository extends JpaRepository<NonconformityTemplate, UUID> {
  Optional<NonconformityTemplate> findByCode(String code);

  List<NonconformityTemplate> findByEnabledTrue();
}


