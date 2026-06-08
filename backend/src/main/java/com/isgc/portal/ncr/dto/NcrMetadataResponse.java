package com.isgc.portal.ncr.dto;

import com.isgc.portal.ncr.NcrMetadata;
import java.util.List;

public record NcrMetadataResponse(
    List<NcrMetadata.Option> rootCauseCategories,
    List<NcrMetadata.Option> isoStandards,
    List<NcrMetadata.Option> statuses,
    List<NcrMetadata.Option> verificationStatuses
) {}
