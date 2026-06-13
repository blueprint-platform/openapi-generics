package io.github.blueprintplatform.samples.typecoverage.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum CoverageStatus {
  ACTIVE,
  PASSIVE,
  EXPERIMENTAL
}
