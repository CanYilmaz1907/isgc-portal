package com.isgc.portal.nonconformity;

import com.isgc.portal.nonconformity.dto.HazardClassResponse;
import com.isgc.portal.nonconformity.dto.NonconformityResponse;
import com.isgc.portal.nonconformity.dto.NonconformityTemplateRequest;
import com.isgc.portal.nonconformity.dto.NonconformityTemplateResponse;
import com.isgc.portal.nonconformity.dto.NonconformityUpsertRequest;
import com.isgc.portal.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nonconformities")
public class NonconformityController {
  private final NonconformityService service;
  private final HazardClassRepository hazardRepo;
  private final NonconformityTemplateRepository templateRepo;

  public NonconformityController(
      NonconformityService service,
      HazardClassRepository hazardRepo,
      NonconformityTemplateRepository templateRepo
  ) {
    this.service = service;
    this.hazardRepo = hazardRepo;
    this.templateRepo = templateRepo;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<NonconformityResponse> list(@AuthenticationPrincipal @NotNull CurrentUser user) {
    return service.list(user);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public NonconformityResponse get(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return service.get(user, id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public NonconformityResponse create(@Valid @RequestBody NonconformityUpsertRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return service.create(user, req);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public NonconformityResponse update(@PathVariable("id") UUID id, @Valid @RequestBody NonconformityUpsertRequest req, @AuthenticationPrincipal @NotNull CurrentUser user) {
    return service.update(user, id, req);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable("id") UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    service.delete(user, id);
  }

  @GetMapping("/hazard-classes")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<HazardClassResponse> hazardClasses() {
    return hazardRepo.findByEnabledTrue().stream().map(h -> new HazardClassResponse(h.getId(), h.getCode(), h.getName())).toList();
  }

  @GetMapping("/templates")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public List<NonconformityTemplateResponse> templates() {
    return templateRepo.findByEnabledTrue().stream()
        .map(t -> new NonconformityTemplateResponse(t.getId(), t.getCode(), t.getName(), t.getTableSchema(), t.isEnabled()))
        .toList();
  }

  @PostMapping("/templates")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public NonconformityTemplateResponse createTemplate(@Valid @RequestBody NonconformityTemplateRequest req) {
    NonconformityTemplate t = new NonconformityTemplate();
    t.setId(UUID.randomUUID());
    t.setCode(req.code());
    t.setName(req.name());
    t.setTableSchema(req.tableSchemaJson());
    t.setEnabled(req.enabled());
    templateRepo.save(t);
    return new NonconformityTemplateResponse(t.getId(), t.getCode(), t.getName(), t.getTableSchema(), t.isEnabled());
  }
}


