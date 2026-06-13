package io.github.blueprintplatform.samples.typecoverage.client.adapter.impl;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.client.adapter.TypeCoverageClientAdapter;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ObjectPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.PagedPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ScalarPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ValuePayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeSummaryDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TypeCoverageClientAdapterImpl implements TypeCoverageClientAdapter {

  private final ScalarPayloadControllerApi scalarApi;
  private final ValuePayloadControllerApi valueApi;
  private final ObjectPayloadControllerApi objectApi;
  private final PagedPayloadControllerApi pagedApi;

  public TypeCoverageClientAdapterImpl(
      ScalarPayloadControllerApi scalarApi,
      ValuePayloadControllerApi valueApi,
      ObjectPayloadControllerApi objectApi,
      PagedPayloadControllerApi pagedApi) {
    this.scalarApi = scalarApi;
    this.valueApi = valueApi;
    this.objectApi = objectApi;
    this.pagedApi = pagedApi;
  }

  @Override
  public ServiceResponse<String> stringValue() {
    return scalarApi.stringValue();
  }

  @Override
  public ServiceResponse<Boolean> booleanValue() {
    return scalarApi.booleanValue();
  }

  @Override
  public ServiceResponse<Integer> integerValue() {
    return scalarApi.integerValue();
  }

  @Override
  public ServiceResponse<Long> longValue() {
    return scalarApi.longValue();
  }

  @Override
  public ServiceResponse<BigDecimal> decimalValue() {
    return scalarApi.decimalValue();
  }

  @Override
  public ServiceResponse<UUID> uuidValue() {
    return valueApi.uuidValue();
  }

  @Override
  public ServiceResponse<LocalDate> dateValue() {
    return valueApi.dateValue();
  }

  @Override
  public ServiceResponse<OffsetDateTime> dateTimeValue() {
    return valueApi.dateTimeValue();
  }

  @Override
  public ServiceResponse<CoverageStatus> enumValue() {
    return valueApi.enumValue();
  }

  @Override
  public ServiceResponse<AddressDto> address() {
    return objectApi.address();
  }

  @Override
  public ServiceResponse<TypeProfileDto> profile() {
    return objectApi.profile();
  }

  @Override
  public ServiceResponse<Page<TypeSummaryDto>> summaries() {
    return pagedApi.summaries();
  }

  @Override
  public ServiceResponse<Page<CoverageStatus>> statuses() {
    return pagedApi.statuses();
  }
}
