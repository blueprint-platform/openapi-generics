package io.github.blueprintplatform.samples.typecoverage.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TypeProfileDto(
    UUID id,
    String code,
    Boolean enabled,
    Integer priority,
    Long version,
    BigDecimal score,
    LocalDate effectiveDate,
    OffsetDateTime updatedAt,
    CoverageStatus status,
    AddressDto address,
    List<String> tags,
    Map<String, String> attributes) {}
