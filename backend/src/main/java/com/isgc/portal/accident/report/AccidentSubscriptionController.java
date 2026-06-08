package com.isgc.portal.accident.report;

import com.isgc.portal.accident.report.dto.SubscriptionResponse;
import com.isgc.portal.accident.report.dto.SubscriptionUpsertRequest;
import com.isgc.portal.project.ProjectRepository;
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
@RequestMapping("/api/accidents/report-subscriptions")
public class AccidentSubscriptionController {
  private final AccidentReportSubscriptionRepository repo;
  private final ProjectRepository projectRepo;

  public AccidentSubscriptionController(AccidentReportSubscriptionRepository repo, ProjectRepository projectRepo) {
    this.repo = repo;
    this.projectRepo = projectRepo;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public List<SubscriptionResponse> list() {
    return repo.findAll().stream().map(this::toDto).toList();
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public SubscriptionResponse create(@Valid @RequestBody SubscriptionUpsertRequest req) {
    AccidentReportSubscription s = new AccidentReportSubscription();
    s.setId(UUID.randomUUID());
    apply(req, s);
    repo.save(s);
    return toDto(s);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public SubscriptionResponse update(@PathVariable UUID id, @Valid @RequestBody SubscriptionUpsertRequest req) {
    AccidentReportSubscription s = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
    apply(req, s);
    repo.save(s);
    return toDto(s);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal @NotNull CurrentUser user) {
    repo.deleteById(id);
  }

  private void apply(SubscriptionUpsertRequest req, AccidentReportSubscription s) {
    if (req.projectId() != null) {
      s.setProject(projectRepo.findById(req.projectId()).orElseThrow(() -> new IllegalArgumentException("Project not found")));
    } else {
      s.setProject(null);
    }
    s.setEnabled(req.enabled());
    s.setFrequency(req.frequency());
    s.setHourOfDay(req.hourOfDay());
    s.setMinuteOfHour(req.minuteOfHour());
    s.setToEmails(req.toEmails());
    s.setCcEmails(req.ccEmails());
    s.setFilters(req.filtersJson() == null || req.filtersJson().isBlank() ? "{}" : req.filtersJson());
  }

  private SubscriptionResponse toDto(AccidentReportSubscription s) {
    return new SubscriptionResponse(
        s.getId(),
        s.getProject() != null ? s.getProject().getId() : null,
        s.isEnabled(),
        s.getFrequency(),
        s.getHourOfDay(),
        s.getMinuteOfHour(),
        s.getToEmails(),
        s.getCcEmails(),
        s.getFilters()
    );
  }
}


