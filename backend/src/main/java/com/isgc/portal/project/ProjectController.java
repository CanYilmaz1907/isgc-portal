package com.isgc.portal.project;

import com.isgc.portal.project.dto.ProjectRequest;
import com.isgc.portal.project.dto.ProjectResponse;
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
@RequestMapping("/api/projects")
public class ProjectController {
  private final ProjectRepository projectRepository;

  public ProjectController(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','READ_ONLY')")
  public List<ProjectResponse> list() {
    return projectRepository.findAll().stream()
        .map(p -> new ProjectResponse(p.getId(), p.getCode(), p.getName(), p.isEnabled()))
        .toList();
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public ProjectResponse create(@Valid @RequestBody ProjectRequest req) {
    Project p = new Project();
    p.setId(UUID.randomUUID());
    p.setCode(req.code());
    p.setName(req.name());
    p.setEnabled(req.enabled());
    projectRepository.save(p);
    return new ProjectResponse(p.getId(), p.getCode(), p.getName(), p.isEnabled());
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C')")
  public ProjectResponse update(@PathVariable UUID id, @Valid @RequestBody ProjectRequest req) {
    Project p = projectRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found"));
    p.setCode(req.code());
    p.setName(req.name());
    p.setEnabled(req.enabled());
    projectRepository.save(p);
    return new ProjectResponse(p.getId(), p.getCode(), p.getName(), p.isEnabled());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable UUID id) {
    projectRepository.deleteById(id);
  }
}


