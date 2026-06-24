package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.samples.typecoverage.api.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.api.dto.TypeProfileDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/types/objects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ObjectPayloadController {

  @GetMapping("/address")
  public ResponseEntity<ServiceResponse<AddressDto>> address() {
    return ResponseEntity.ok(ServiceResponse.of(new AddressDto("Istanbul", "TR", "34700")));
  }

  @GetMapping("/profile")
  public ResponseEntity<ServiceResponse<TypeProfileDto>> profile() {
    var dto =
        new TypeProfileDto(
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            "TYPE-COVERAGE-001",
            true,
            1,
            3L,
            new BigDecimal("98.75"),
            LocalDate.of(2026, Month.JUNE, 10),
            OffsetDateTime.parse("2026-06-10T21:00:00+03:00"),
            CoverageStatus.ACTIVE,
            new AddressDto("Istanbul", "TR", "34700"),
            List.of("scalar", "value", "object"),
            Map.of("envelope", "ServiceResponse", "sample", "type-coverage"));

    return ResponseEntity.ok(ServiceResponse.of(dto));
  }
}
