package com.isgc.portal.audit;

import com.isgc.portal.audit.dto.ChecklistItemResponse;
import com.isgc.portal.audit.dto.ChecklistItemUpsertRequest;
import com.isgc.portal.audit.dto.ChecklistResponse;
import com.isgc.portal.audit.dto.ChecklistUpsertRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {
  private final ChecklistRepository checklistRepo;
  private final ChecklistItemRepository itemRepo;

  public ChecklistController(ChecklistRepository checklistRepo, ChecklistItemRepository itemRepo) {
    this.checklistRepo = checklistRepo;
    this.itemRepo = itemRepo;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<ChecklistResponse> list() {
    return checklistRepo.findAll().stream()
        .map(c -> new ChecklistResponse(c.getId(), c.getCode(), c.getTitle(), c.getScope(), c.isEnabled()))
        .toList();
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public ChecklistResponse create(@Valid @RequestBody ChecklistUpsertRequest req) {
    Checklist c = new Checklist();
    c.setId(UUID.randomUUID());
    c.setCode(req.code());
    c.setTitle(req.title());
    c.setScope(req.scope() == null || req.scope().isBlank() ? "GENERAL" : req.scope());
    c.setEnabled(req.enabled());
    checklistRepo.save(c);
    return new ChecklistResponse(c.getId(), c.getCode(), c.getTitle(), c.getScope(), c.isEnabled());
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public ChecklistResponse update(@PathVariable("id") UUID id, @Valid @RequestBody ChecklistUpsertRequest req) {
    Checklist c = checklistRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Checklist not found"));
    c.setCode(req.code());
    c.setTitle(req.title());
    c.setScope(req.scope() == null || req.scope().isBlank() ? "GENERAL" : req.scope());
    c.setEnabled(req.enabled());
    checklistRepo.save(c);
    return new ChecklistResponse(c.getId(), c.getCode(), c.getTitle(), c.getScope(), c.isEnabled());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable("id") UUID id) {
    checklistRepo.deleteById(id);
  }

  @GetMapping("/{checklistId}/items")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<ChecklistItemResponse> items(@PathVariable("checklistId") UUID checklistId) {
    return itemRepo.findByChecklistId(checklistId).stream()
        .map(this::toItemResponse)
        .toList();
  }

  @PostMapping("/{checklistId}/items")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public ChecklistItemResponse addItem(
      @PathVariable("checklistId") UUID checklistId,
      @Valid @RequestBody ChecklistItemUpsertRequest req) {
    ChecklistItem item = createOrUpdateItem(checklistId, null, req);
    return toItemResponse(item);
  }

  @PutMapping("/{checklistId}/items/{itemId}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public ChecklistItemResponse updateItem(
      @PathVariable("checklistId") UUID checklistId,
      @PathVariable("itemId") UUID itemId,
      @Valid @RequestBody ChecklistItemUpsertRequest req) {
    ChecklistItem item = createOrUpdateItem(checklistId, itemId, req);
    return toItemResponse(item);
  }

  @DeleteMapping("/{checklistId}/items/{itemId}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public void deleteItem(@PathVariable("checklistId") UUID checklistId, @PathVariable("itemId") UUID itemId) {
    ChecklistItem item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Checklist item not found"));
    if (!item.getChecklist().getId().equals(checklistId)) {
      throw new IllegalArgumentException("Checklist mismatch");
    }
    itemRepo.delete(item);
  }

  private ChecklistItem createOrUpdateItem(UUID checklistId, UUID itemId, ChecklistItemUpsertRequest req) {
    if (!checklistId.equals(req.checklistId())) {
      throw new IllegalArgumentException("Checklist mismatch");
    }
    Checklist checklist = checklistRepo.findById(checklistId).orElseThrow(() -> new IllegalArgumentException("Checklist not found"));

    ChecklistItem item;
    if (itemId == null) {
      item = new ChecklistItem();
      item.setId(UUID.randomUUID());
      item.setChecklist(checklist);
    } else {
      item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Checklist item not found"));
      if (!item.getChecklist().getId().equals(checklistId)) {
        throw new IllegalArgumentException("Checklist mismatch");
      }
    }

    item.setItemNo(req.itemNo());
    item.setQuestion(req.question());
    item.setWeight(req.weight());
    item.setMaxScore(req.maxScore());
    item.setEnabled(req.enabled());
    item.setCategoryNo(ChecklistCategoryUtil.resolveCategoryNo(req.itemNo(), req.categoryNo()));
    return itemRepo.save(item);
  }

  private ChecklistItemResponse toItemResponse(ChecklistItem item) {
    return new ChecklistItemResponse(
        item.getId(),
        item.getChecklist().getId(),
        item.getItemNo(),
        item.getCategoryNo(),
        item.getQuestion(),
        item.getWeight(),
        item.getMaxScore(),
        item.isEnabled());
  }
}
