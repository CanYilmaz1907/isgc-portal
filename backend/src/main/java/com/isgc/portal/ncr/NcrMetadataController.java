package com.isgc.portal.ncr;

import com.isgc.portal.ncr.dto.NcrMetadataResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ncr/metadata")
public class NcrMetadataController {
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ISG_C','YONETICI','PERSONEL','READ_ONLY')")
  public NcrMetadataResponse metadata() {
    return new NcrMetadataResponse(
        NcrMetadata.rootCauseCategories(),
        NcrMetadata.isoStandards(),
        NcrMetadata.statuses(),
        NcrMetadata.verificationStatuses()
    );
  }
}
