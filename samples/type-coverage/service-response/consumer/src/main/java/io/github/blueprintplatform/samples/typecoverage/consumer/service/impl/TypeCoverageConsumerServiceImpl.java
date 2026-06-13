package io.github.blueprintplatform.samples.typecoverage.consumer.service.impl;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.client.adapter.TypeCoverageClientAdapter;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.consumer.service.TypeCoverageConsumerService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TypeCoverageConsumerServiceImpl implements TypeCoverageConsumerService {

  private final TypeCoverageClientAdapter adapter;

  public TypeCoverageConsumerServiceImpl(TypeCoverageClientAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public ServiceResponse<String> stringValue() {
    return adapter.stringValue();
  }

  @Override
  public ServiceResponse<Boolean> booleanValue() {
    return adapter.booleanValue();
  }

  @Override
  public ServiceResponse<Integer> integerValue() {
    return adapter.integerValue();
  }

  @Override
  public ServiceResponse<Long> longValue() {
    return adapter.longValue();
  }

  @Override
  public ServiceResponse<BigDecimal> decimalValue() {
    return adapter.decimalValue();
  }

  @Override
  public ServiceResponse<UUID> uuidValue() {
    return adapter.uuidValue();
  }

  @Override
  public ServiceResponse<LocalDate> dateValue() {
    return adapter.dateValue();
  }

  @Override
  public ServiceResponse<OffsetDateTime> dateTimeValue() {
    return adapter.dateTimeValue();
  }

  @Override
  public ServiceResponse<CoverageStatus> enumValue() {
    return adapter.enumValue();
  }

  @Override
  public ServiceResponse<AddressDto> address() {
    return adapter.address();
  }

  @Override
  public ServiceResponse<TypeProfileDto> profile() {
    return adapter.profile();
  }

  @Override
  public ServiceResponse<Page<TypeSummaryDto>> summaries() {
    return adapter.summaries();
  }

  @Override
  public ServiceResponse<Page<CoverageStatus>> statuses() {
    return adapter.statuses();
  }
}
