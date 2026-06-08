package com.isgc.portal.accident;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(20)
public class AccidentMetadataSeeder implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(AccidentMetadataSeeder.class);

  private final CauseCategoryRepository causeCategoryRepo;
  private final AccidentLookupOptionRepository lookupRepo;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AccidentMetadataSeeder(CauseCategoryRepository causeCategoryRepo, AccidentLookupOptionRepository lookupRepo) {
    this.causeCategoryRepo = causeCategoryRepo;
    this.lookupRepo = lookupRepo;
  }

  @Override
  @Transactional
  public void run(String... args) {
    seedLookupOptions();
    seedCauseCategories();
  }

  private void seedLookupOptions() {
    if (lookupRepo.count() > 0) {
      return;
    }
    try (InputStream in = new ClassPathResource("accident/lookup-options.json").getInputStream()) {
      JsonNode root = objectMapper.readTree(in);
      int order = 0;
      for (var typeEntry : root.properties()) {
        String type = typeEntry.getKey();
        for (JsonNode item : typeEntry.getValue()) {
          AccidentLookupOption opt = new AccidentLookupOption();
          opt.setId(UUID.randomUUID());
          opt.setOptionType(type);
          opt.setOptionCode(item.get("code").asText());
          opt.setOptionLabel(item.get("label").asText());
          opt.setSortOrder(order++);
          lookupRepo.save(opt);
        }
      }
      log.info("AccidentMetadataSeeder: lookup options seeded.");
    } catch (Exception e) {
      log.error("Failed to seed accident lookup options", e);
    }
  }

  private void seedCauseCategories() {
    if (causeCategoryRepo.countByCauseType("DIRECT") > 0) {
      return;
    }
    try (InputStream in = new ClassPathResource("accident/cause-categories.json").getInputStream()) {
      JsonNode root = objectMapper.readTree(in);
      seedCauseType(root.get("direct"), "DIRECT");
      seedCauseType(root.get("root"), "ROOT");
      log.info("AccidentMetadataSeeder: cause categories seeded.");
    } catch (Exception e) {
      log.error("Failed to seed cause categories", e);
    }
  }

  private void seedCauseType(JsonNode groups, String causeType) {
    int sort = 0;
    for (JsonNode group : groups) {
      String section = group.get("section").asText();
      String groupCode = group.get("groupCode").asText();
      String groupName = group.get("groupName").asText();
      for (JsonNode item : group.get("items")) {
        CauseCategory c = new CauseCategory();
        c.setId(UUID.randomUUID());
        c.setCauseType(causeType);
        c.setSection(section);
        c.setGroupCode(groupCode);
        c.setGroupName(groupName);
        c.setItemCode(item.get("code").asText());
        c.setItemLabel(item.get("label").asText());
        c.setSortOrder(sort++);
        causeCategoryRepo.save(c);
      }
    }
  }
}
