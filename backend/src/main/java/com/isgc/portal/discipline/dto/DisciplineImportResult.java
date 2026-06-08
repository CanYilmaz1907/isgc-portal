package com.isgc.portal.discipline.dto;

import java.util.List;

public record DisciplineImportResult(int imported, List<String> errors) {}
