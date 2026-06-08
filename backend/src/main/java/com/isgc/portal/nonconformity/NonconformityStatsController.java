package com.isgc.portal.nonconformity;

import com.isgc.portal.nonconformity.dto.NonconformityProjectStatsDto;
import com.isgc.portal.project.Project;
import com.isgc.portal.project.ProjectRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nonconformities/stats")
public class NonconformityStatsController {
  private final NonconformityRepository repo;
  private final ProjectRepository projectRepo;

  public NonconformityStatsController(NonconformityRepository repo, ProjectRepository projectRepo) {
    this.repo = repo;
    this.projectRepo = projectRepo;
  }

  /** Total or filtered by project: hazard class counts. */
  @GetMapping("/by-hazard-class")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public Map<String, Long> byHazardClass(@RequestParam(required = false) UUID projectId) {
    Map<String, Long> out = new HashMap<>();
    List<Nonconformity> list = projectId != null ? repo.findByProjectId(projectId) : repo.findAll();
    for (Nonconformity n : list) {
      String key = n.getHazardClass() != null ? n.getHazardClass().getName() : "Tanımsız";
      out.put(key, out.getOrDefault(key, 0L) + 1L);
    }
    return out;
  }

  /** Per-project stats: one entry per project with hazard class counts. */
  @GetMapping("/by-project")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public List<NonconformityProjectStatsDto> byProject() {
    List<Project> projects = projectRepo.findAll();
    List<Nonconformity> all = repo.findAll();
    return projects.stream()
        .map(p -> {
          Map<String, Long> byHazard = new HashMap<>();
          for (Nonconformity n : all) {
            if (n.getProject() != null && n.getProject().getId().equals(p.getId())) {
              String key = n.getHazardClass() != null ? n.getHazardClass().getName() : "Tanımsız";
              byHazard.put(key, byHazard.getOrDefault(key, 0L) + 1L);
            }
          }
          return new NonconformityProjectStatsDto(p.getId(), p.getName(), byHazard);
        })
        .toList();
  }
}


