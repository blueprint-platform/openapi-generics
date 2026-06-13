package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
import java.time.LocalDate;
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
  public ResponseEntity<ApiResponse<UUID>> uuidValue() {
    return ResponseEntity.ok(
        ApiResponse.ok(UUID.fromString("11111111-1111-1111-1111-111111111111")));
  }

  @GetMapping("/date")
  public ResponseEntity<ApiResponse<LocalDate>> dateValue() {
    return ResponseEntity.ok(ApiResponse.ok(LocalDate.of(2026, 6, 10)));
  }

  @GetMapping("/datetime")
  public ResponseEntity<ApiResponse<OffsetDateTime>> dateTimeValue() {
    return ResponseEntity.ok(ApiResponse.ok(OffsetDateTime.parse("2026-06-10T21:00:00+03:00")));
  }

  @GetMapping("/enum")
  public ResponseEntity<ApiResponse<CoverageStatus>> enumValue() {
    return ResponseEntity.ok(ApiResponse.ok(CoverageStatus.EXPERIMENTAL));
  }
}
