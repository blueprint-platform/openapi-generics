package io.github.blueprintplatform.samples.typecoverage.client.adapter.impl;

import io.github.blueprintplatform.samples.typecoverage.client.adapter.TypeCoverageClientAdapter;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ObjectPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ScalarPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ValuePayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
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

  public TypeCoverageClientAdapterImpl(
      ScalarPayloadControllerApi scalarApi,
      ValuePayloadControllerApi valueApi,
      ObjectPayloadControllerApi objectApi) {
    this.scalarApi = scalarApi;
    this.valueApi = valueApi;
    this.objectApi = objectApi;
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
}
