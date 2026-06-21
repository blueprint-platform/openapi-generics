package io.github.blueprintplatform.samples.typecoverage.api.dto;

import java.util.UUID;

public record TypeSummaryDto(UUID id, String code, CoverageStatus status) {}
