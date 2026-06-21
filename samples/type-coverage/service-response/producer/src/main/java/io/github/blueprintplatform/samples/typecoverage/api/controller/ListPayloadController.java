package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.api.dto.TypeSummaryDto;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/types/lists", produces = MediaType.APPLICATION_JSON_VALUE)
public class ListPayloadController {

  @GetMapping("/summaries")
  public ResponseEntity<ServiceResponse<List<TypeSummaryDto>>> listSummaries() {
    var content =
        List.of(
            new TypeSummaryDto(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "TYPE-SUMMARY-001",
                CoverageStatus.ACTIVE),
            new TypeSummaryDto(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "TYPE-SUMMARY-002",
                CoverageStatus.EXPERIMENTAL));

    var meta = Meta.now("code", SortDirection.ASC);

    return ResponseEntity.ok(ServiceResponse.of(content, meta));
  }

  @GetMapping("/statuses")
  public ResponseEntity<ServiceResponse<List<CoverageStatus>>> listStatuses() {
    var content =
        List.of(CoverageStatus.ACTIVE, CoverageStatus.PASSIVE, CoverageStatus.EXPERIMENTAL);

    var meta = Meta.now("status", SortDirection.ASC);

    return ResponseEntity.ok(ServiceResponse.of(content, meta));
  }
}
