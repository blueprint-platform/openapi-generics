package io.github.blueprintplatform.samples.typecoverage.consumer.service.impl;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.client.adapter.TypeCoverageClientAdapter;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.consumer.service.TypeCoverageConsumerService;
import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
import io.github.blueprintplatform.samples.typecoverage.contract.Paging;
import io.github.blueprintplatform.samples.typecoverage.contract.Window;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TypeCoverageConsumerServiceImpl implements TypeCoverageConsumerService {

  private final TypeCoverageClientAdapter adapter;

  public TypeCoverageConsumerServiceImpl(TypeCoverageClientAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public ApiResponse<String> stringValue() {
    return adapter.stringValue();
  }

  @Override
  public ApiResponse<Boolean> booleanValue() {
    return adapter.booleanValue();
  }

  @Override
  public ApiResponse<Integer> integerValue() {
    return adapter.integerValue();
  }

  @Override
  public ApiResponse<Long> longValue() {
    return adapter.longValue();
  }

  @Override
  public ApiResponse<BigDecimal> decimalValue() {
    return adapter.decimalValue();
  }

  @Override
  public ApiResponse<UUID> uuidValue() {
    return adapter.uuidValue();
  }

  @Override
  public ApiResponse<LocalDate> dateValue() {
    return adapter.dateValue();
  }

  @Override
  public ApiResponse<OffsetDateTime> dateTimeValue() {
    return adapter.dateTimeValue();
  }

  @Override
  public ApiResponse<CoverageStatus> enumValue() {
    return adapter.enumValue();
  }

  @Override
  public ApiResponse<AddressDto> address() {
    return adapter.address();
  }

  @Override
  public ApiResponse<TypeProfileDto> profile() {
    return adapter.profile();
  }

  @Override
  public ApiResponse<List<TypeSummaryDto>> listSummaries() {
    return adapter.listSummaries();
  }

  @Override
  public ApiResponse<List<CoverageStatus>> listStatuses() {
    return adapter.listStatuses();
  }

  @Override
  public ApiResponse<Set<TypeSummaryDto>> setSummaries() {
    return adapter.setSummaries();
  }

  @Override
  public ApiResponse<Set<CoverageStatus>> setStatuses() {
    return adapter.setStatuses();
  }

  @Override
  public ApiResponse<Page<TypeSummaryDto>> pagedSummaries() {
    return adapter.pagedSummaries();
  }

  @Override
  public ApiResponse<Page<CoverageStatus>> pagedStatuses() {
    return adapter.pagedStatuses();
  }

  @Override
  public ApiResponse<Paging<TypeSummaryDto>> pagingSummaries() {
    return adapter.pagingSummaries();
  }

  @Override
  public ApiResponse<Paging<CoverageStatus>> pagingStatuses() {
    return adapter.pagingStatuses();
  }

  @Override
  public ApiResponse<Window<TypeSummaryDto>> windowSummaries() {
    return adapter.windowSummaries();
  }

  @Override
  public ApiResponse<Window<CoverageStatus>> windowStatuses() {
    return adapter.windowStatuses();
  }
}
