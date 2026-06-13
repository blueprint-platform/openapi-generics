package io.github.blueprintplatform.samples.typecoverage.consumer.api.controller;

import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.consumer.service.TypeCoverageConsumerService;
import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
public class TypeCoverageConsumerController {

  private final TypeCoverageConsumerService service;

  public TypeCoverageConsumerController(TypeCoverageConsumerService service) {
    this.service = service;
  }

  @GetMapping("/scalars/string")
  public ResponseEntity<ApiResponse<String>> stringValue() {
    return ResponseEntity.ok(service.stringValue());
  }

  @GetMapping("/scalars/boolean")
  public ResponseEntity<ApiResponse<Boolean>> booleanValue() {
    return ResponseEntity.ok(service.booleanValue());
  }

  @GetMapping("/scalars/integer")
  public ResponseEntity<ApiResponse<Integer>> integerValue() {
    return ResponseEntity.ok(service.integerValue());
  }

  @GetMapping("/scalars/long")
  public ResponseEntity<ApiResponse<Long>> longValue() {
    return ResponseEntity.ok(service.longValue());
  }

  @GetMapping("/scalars/decimal")
  public ResponseEntity<ApiResponse<BigDecimal>> decimalValue() {
    return ResponseEntity.ok(service.decimalValue());
  }

  @GetMapping("/values/uuid")
  public ResponseEntity<ApiResponse<UUID>> uuidValue() {
    return ResponseEntity.ok(service.uuidValue());
  }

  @GetMapping("/values/date")
  public ResponseEntity<ApiResponse<LocalDate>> dateValue() {
    return ResponseEntity.ok(service.dateValue());
  }

  @GetMapping("/values/datetime")
  public ResponseEntity<ApiResponse<OffsetDateTime>> dateTimeValue() {
    return ResponseEntity.ok(service.dateTimeValue());
  }

  @GetMapping("/values/enum")
  public ResponseEntity<ApiResponse<CoverageStatus>> enumValue() {
    return ResponseEntity.ok(service.enumValue());
  }

  @GetMapping("/objects/address")
  public ResponseEntity<ApiResponse<AddressDto>> address() {
    return ResponseEntity.ok(service.address());
  }

  @GetMapping("/objects/profile")
  public ResponseEntity<ApiResponse<TypeProfileDto>> profile() {
    return ResponseEntity.ok(service.profile());
  }
}
