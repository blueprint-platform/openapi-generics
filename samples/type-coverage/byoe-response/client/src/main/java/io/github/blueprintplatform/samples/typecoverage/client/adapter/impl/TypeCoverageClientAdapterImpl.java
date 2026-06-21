package io.github.blueprintplatform.samples.typecoverage.client.adapter.impl;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.client.adapter.TypeCoverageClientAdapter;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ListPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ObjectPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.PagedPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ScalarPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.SetPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ValuePayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TypeCoverageClientAdapterImpl implements TypeCoverageClientAdapter {

  private final ScalarPayloadControllerApi scalarApi;
  private final ValuePayloadControllerApi valueApi;
  private final ObjectPayloadControllerApi objectApi;
  private final ListPayloadControllerApi listApi;
  private final SetPayloadControllerApi setApi;
  private final PagedPayloadControllerApi pagedApi;

  public TypeCoverageClientAdapterImpl(
      ScalarPayloadControllerApi scalarApi,
      ValuePayloadControllerApi valueApi,
      ObjectPayloadControllerApi objectApi,
      ListPayloadControllerApi listApi,
      SetPayloadControllerApi setApi,
      PagedPayloadControllerApi pagedApi) {
    this.scalarApi = scalarApi;
    this.valueApi = valueApi;
    this.objectApi = objectApi;
    this.listApi = listApi;
    this.setApi = setApi;
    this.pagedApi = pagedApi;
  }

  @Override
  public ApiResponse<String> stringValue() {
    return scalarApi.stringValue();
  }

  @Override
  public ApiResponse<Boolean> booleanValue() {
    return scalarApi.booleanValue();
  }

  @Override
  public ApiResponse<Integer> integerValue() {
    return scalarApi.integerValue();
  }

  @Override
  public ApiResponse<Long> longValue() {
    return scalarApi.longValue();
  }

  @Override
  public ApiResponse<BigDecimal> decimalValue() {
    return scalarApi.decimalValue();
  }

  @Override
  public ApiResponse<UUID> uuidValue() {
    return valueApi.uuidValue();
  }

  @Override
  public ApiResponse<LocalDate> dateValue() {
    return valueApi.dateValue();
  }

  @Override
  public ApiResponse<OffsetDateTime> dateTimeValue() {
    return valueApi.dateTimeValue();
  }

  @Override
  public ApiResponse<CoverageStatus> enumValue() {
    return valueApi.enumValue();
  }

  @Override
  public ApiResponse<AddressDto> address() {
    return objectApi.address();
  }

  @Override
  public ApiResponse<TypeProfileDto> profile() {
    return objectApi.profile();
  }

  @Override
  public ApiResponse<List<TypeSummaryDto>> listSummaries() {
    return listApi.listSummaries();
  }

  @Override
  public ApiResponse<List<CoverageStatus>> listStatuses() {
    return listApi.listStatuses();
  }

  @Override
  public ApiResponse<Set<TypeSummaryDto>> setSummaries() {
    return setApi.setSummaries();
  }

  @Override
  public ApiResponse<Set<CoverageStatus>> setStatuses() {
    return setApi.setStatuses();
  }

  @Override
  public ApiResponse<Page<TypeSummaryDto>> pagedSummaries() {
    return pagedApi.pagedSummaries();
  }

  @Override
  public ApiResponse<Page<CoverageStatus>> pagedStatuses() {
    return pagedApi.pagedStatuses();
  }
}
