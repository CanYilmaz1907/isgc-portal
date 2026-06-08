package com.isgc.portal.accident;

import com.isgc.portal.accident.dto.AccidentMetadataResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accidents")
public class AccidentMetadataController {
  private final AccidentMetadataService metadataService;

  public AccidentMetadataController(AccidentMetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @GetMapping("/metadata")
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public AccidentMetadataResponse metadata() {
    return metadataService.metadata();
  }
}
