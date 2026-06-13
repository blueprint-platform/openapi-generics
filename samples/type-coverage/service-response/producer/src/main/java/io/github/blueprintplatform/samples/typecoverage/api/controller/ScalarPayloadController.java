package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import java.math.BigDecimal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/types/scalars", produces = MediaType.APPLICATION_JSON_VALUE)
public class ScalarPayloadController {

  @GetMapping("/string")
  public ResponseEntity<ServiceResponse<String>> stringValue() {
    return ResponseEntity.ok(ServiceResponse.of("type-coverage"));
  }

  @GetMapping("/boolean")
  public ResponseEntity<ServiceResponse<Boolean>> booleanValue() {
    return ResponseEntity.ok(ServiceResponse.of(Boolean.TRUE));
  }

  @GetMapping("/integer")
  public ResponseEntity<ServiceResponse<Integer>> integerValue() {
    return ResponseEntity.ok(ServiceResponse.of(42));
  }

  @GetMapping("/long")
  public ResponseEntity<ServiceResponse<Long>> longValue() {
    return ResponseEntity.ok(ServiceResponse.of(9007199254740991L));
  }

  @GetMapping("/decimal")
  public ResponseEntity<ServiceResponse<BigDecimal>> decimalValue() {
    return ResponseEntity.ok(ServiceResponse.of(new BigDecimal("12345.67")));
  }
}
