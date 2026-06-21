package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.api.dto.TypeSummaryDto;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/types/sets", produces = MediaType.APPLICATION_JSON_VALUE)
public class SetPayloadController {

    @GetMapping("/summaries")
    public ResponseEntity<ServiceResponse<Set<TypeSummaryDto>>> setSummaries() {
        var content =
                Set.of(
                        new TypeSummaryDto(
                                UUID.fromString("99999999-9999-9999-9999-999999999999"),
                                "TYPE-SET-001",
                                CoverageStatus.ACTIVE),
                        new TypeSummaryDto(
                                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                                "TYPE-SET-002",
                                CoverageStatus.EXPERIMENTAL));

        var meta = Meta.now("code", SortDirection.ASC);

        return ResponseEntity.ok(ServiceResponse.of(content, meta));
    }

    @GetMapping("/statuses")
    public ResponseEntity<ServiceResponse<Set<CoverageStatus>>> setStatuses() {
        var content =
                Set.of(
                        CoverageStatus.ACTIVE,
                        CoverageStatus.PASSIVE,
                        CoverageStatus.EXPERIMENTAL);

        var meta = Meta.now("status", SortDirection.ASC);

        return ResponseEntity.ok(ServiceResponse.of(content, meta));
    }
}