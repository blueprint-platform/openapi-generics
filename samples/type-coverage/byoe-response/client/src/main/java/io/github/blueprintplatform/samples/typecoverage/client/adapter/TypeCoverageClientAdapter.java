package io.github.blueprintplatform.samples.typecoverage.client.adapter;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
import io.github.blueprintplatform.samples.typecoverage.contract.Paging;
import io.github.blueprintplatform.samples.typecoverage.contract.Window;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
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

  ApiResponse<List<TypeSummaryDto>> listSummaries();

  ApiResponse<List<CoverageStatus>> listStatuses();

  ApiResponse<Set<TypeSummaryDto>> setSummaries();

  ApiResponse<Set<CoverageStatus>> setStatuses();

  ApiResponse<Page<TypeSummaryDto>> pagedSummaries();

  ApiResponse<Page<CoverageStatus>> pagedStatuses();

  ApiResponse<Paging<TypeSummaryDto>> pagingSummaries();

  ApiResponse<Paging<CoverageStatus>> pagingStatuses();

  ApiResponse<Window<TypeSummaryDto>> windowSummaries();

  ApiResponse<Window<CoverageStatus>> windowStatuses();
}
