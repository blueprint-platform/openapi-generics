package io.github.blueprintplatform.samples.typecoverage.client.adapter;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeSummaryDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface TypeCoverageClientAdapter {

  ServiceResponse<String> stringValue();

  ServiceResponse<Boolean> booleanValue();

  ServiceResponse<Integer> integerValue();

  ServiceResponse<Long> longValue();

  ServiceResponse<BigDecimal> decimalValue();

  ServiceResponse<UUID> uuidValue();

  ServiceResponse<LocalDate> dateValue();

  ServiceResponse<OffsetDateTime> dateTimeValue();

  ServiceResponse<CoverageStatus> enumValue();

  ServiceResponse<AddressDto> address();

  ServiceResponse<TypeProfileDto> profile();

  ServiceResponse<Page<TypeSummaryDto>> summaries();

  ServiceResponse<Page<CoverageStatus>> statuses();
}
