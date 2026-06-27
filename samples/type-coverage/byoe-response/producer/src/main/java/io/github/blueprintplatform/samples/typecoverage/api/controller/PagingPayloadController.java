package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.api.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
import io.github.blueprintplatform.samples.typecoverage.contract.Paging;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/types/paging", produces = MediaType.APPLICATION_JSON_VALUE)
public class PagingPayloadController {

  @GetMapping("/summaries")
  public ResponseEntity<ApiResponse<Paging<TypeSummaryDto>>> pagingSummaries() {
    var content =
        List.of(
            new TypeSummaryDto(
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                "BYOE-PAGING-001",
                CoverageStatus.ACTIVE),
            new TypeSummaryDto(
                UUID.fromString("66666666-6666-6666-6666-666666666666"),
                "BYOE-PAGING-002",
                CoverageStatus.EXPERIMENTAL));

    return ResponseEntity.ok(ApiResponse.ok(Paging.of(content, 0, 2, 2)));
  }

  @GetMapping("/statuses")
  public ResponseEntity<ApiResponse<Paging<CoverageStatus>>> pagingStatuses() {
    var content =
        List.of(CoverageStatus.ACTIVE, CoverageStatus.PASSIVE, CoverageStatus.EXPERIMENTAL);

    return ResponseEntity.ok(ApiResponse.ok(Paging.of(content, 0, 3, 3)));
  }
}
