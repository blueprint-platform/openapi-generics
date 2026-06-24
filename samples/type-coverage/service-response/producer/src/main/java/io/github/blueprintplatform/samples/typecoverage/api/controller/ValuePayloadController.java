package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/types/values", produces = MediaType.APPLICATION_JSON_VALUE)
public class ValuePayloadController {

  @GetMapping("/uuid")
  public ResponseEntity<ServiceResponse<UUID>> uuidValue() {
    return ResponseEntity.ok(
        ServiceResponse.of(UUID.fromString("11111111-1111-1111-1111-111111111111")));
  }

  @GetMapping("/date")
  public ResponseEntity<ServiceResponse<LocalDate>> dateValue() {
    return ResponseEntity.ok(ServiceResponse.of(LocalDate.of(2026, Month.JUNE, 10)));
  }

  @GetMapping("/datetime")
  public ResponseEntity<ServiceResponse<OffsetDateTime>> dateTimeValue() {
    return ResponseEntity.ok(ServiceResponse.of(OffsetDateTime.parse("2026-06-10T21:00:00+03:00")));
  }

  @GetMapping("/enum")
  public ResponseEntity<ServiceResponse<CoverageStatus>> enumValue() {
    return ResponseEntity.ok(ServiceResponse.of(CoverageStatus.EXPERIMENTAL));
  }
}
