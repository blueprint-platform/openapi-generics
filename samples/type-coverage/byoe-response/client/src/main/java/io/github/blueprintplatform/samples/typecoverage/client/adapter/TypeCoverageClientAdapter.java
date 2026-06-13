package io.github.blueprintplatform.samples.typecoverage.client.adapter;

import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface TypeCoverageClientAdapter {

  ApiResponse<String> stringValue();

  ApiResponse<Boolean> booleanValue();

  ApiResponse<Integer> integerValue();

  ApiResponse<Long> longValue();

  ApiResponse<BigDecimal> decimalValue();

  ApiResponse<UUID> uuidValue();

  ApiResponse<LocalDate> dateValue();

  ApiResponse<OffsetDateTime> dateTimeValue();

  ApiResponse<CoverageStatus> enumValue();

  ApiResponse<AddressDto> address();

  ApiResponse<TypeProfileDto> profile();
}
