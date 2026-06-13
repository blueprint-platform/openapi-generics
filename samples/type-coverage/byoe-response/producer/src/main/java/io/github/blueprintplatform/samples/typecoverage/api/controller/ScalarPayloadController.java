package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
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
  public ResponseEntity<ApiResponse<String>> stringValue() {
    return ResponseEntity.ok(ApiResponse.ok("type-coverage"));
  }

  @GetMapping("/boolean")
  public ResponseEntity<ApiResponse<Boolean>> booleanValue() {
    return ResponseEntity.ok(ApiResponse.ok(Boolean.TRUE));
  }

  @GetMapping("/integer")
  public ResponseEntity<ApiResponse<Integer>> integerValue() {
    return ResponseEntity.ok(ApiResponse.ok(42));
  }

  @GetMapping("/long")
  public ResponseEntity<ApiResponse<Long>> longValue() {
    return ResponseEntity.ok(ApiResponse.ok(9007199254740991L));
  }

  @GetMapping("/decimal")
  public ResponseEntity<ApiResponse<BigDecimal>> decimalValue() {
    return ResponseEntity.ok(ApiResponse.ok(new BigDecimal("12345.67")));
  }
}
