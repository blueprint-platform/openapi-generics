package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.api.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/types/pages", produces = MediaType.APPLICATION_JSON_VALUE)
public class PagedPayloadController {

    @GetMapping("/summaries")
    public ResponseEntity<ApiResponse<Page<TypeSummaryDto>>> pagedSummaries() {
        var content =
                List.of(
                        new TypeSummaryDto(
                                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                                "BYOE-PAGE-001",
                                CoverageStatus.ACTIVE),
                        new TypeSummaryDto(
                                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                                "BYOE-PAGE-002",
                                CoverageStatus.EXPERIMENTAL));

        return ResponseEntity.ok(ApiResponse.ok(Page.of(content, 0, 2, 2)));
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<Page<CoverageStatus>>> pagedStatuses() {
        var content =
                List.of(CoverageStatus.ACTIVE, CoverageStatus.PASSIVE, CoverageStatus.EXPERIMENTAL);

        return ResponseEntity.ok(ApiResponse.ok(Page.of(content, 0, 3, 3)));
    }
}