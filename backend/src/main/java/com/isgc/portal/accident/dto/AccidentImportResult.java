package com.isgc.portal.accident.dto;

import java.util.List;

public record AccidentImportResult(int deleted, int imported, List<String> errors) {}
