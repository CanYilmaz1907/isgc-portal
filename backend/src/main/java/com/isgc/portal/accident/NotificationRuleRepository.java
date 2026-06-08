package com.isgc.portal.accident;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRuleRepository extends JpaRepository<NotificationRule, UUID> {
  Optional<NotificationRule> findByAccidentClassAndPotentialLevelAndEnabledTrue(
      AccidentClass accidentClass,
      PotentialLevel potentialLevel
  );
}


