package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.api.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
import io.github.blueprintplatform.samples.typecoverage.contract.Window;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/types/windows", produces = MediaType.APPLICATION_JSON_VALUE)
public class WindowPayloadController {

  @GetMapping("/summaries")
  public ResponseEntity<ApiResponse<Window<TypeSummaryDto>>> windowSummaries() {
    var items =
        List.of(
            new TypeSummaryDto(
                UUID.fromString("77777777-7777-7777-7777-777777777777"),
                "BYOE-WINDOW-001",
                CoverageStatus.ACTIVE),
            new TypeSummaryDto(
                UUID.fromString("88888888-8888-8888-8888-888888888888"),
                "BYOE-WINDOW-002",
                CoverageStatus.EXPERIMENTAL));

    return ResponseEntity.ok(ApiResponse.ok(Window.of(items, "next-window-token", true)));
  }

  @GetMapping("/statuses")
  public ResponseEntity<ApiResponse<Window<CoverageStatus>>> windowStatuses() {
    var items = List.of(CoverageStatus.ACTIVE, CoverageStatus.PASSIVE, CoverageStatus.EXPERIMENTAL);

    return ResponseEntity.ok(ApiResponse.ok(Window.of(items, null, false)));
  }
}
