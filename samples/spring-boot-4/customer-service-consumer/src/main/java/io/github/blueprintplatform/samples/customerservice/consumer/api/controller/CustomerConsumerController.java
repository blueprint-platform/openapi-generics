package io.github.blueprintplatform.samples.customerservice.consumer.api.controller;

import static io.github.blueprintplatform.samples.customerservice.consumer.api.version.ApiVersions.V1;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerSearchCriteria;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerUpdateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.service.CustomerConsumerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes customer operations sourced from the upstream customer service. Delegates all business
 * logic to CustomerConsumerService.
 */
@RestController
@RequestMapping(value = "/customers", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class CustomerConsumerController {

  private final CustomerConsumerService customerConsumerService;

  public CustomerConsumerController(CustomerConsumerService customerConsumerService) {
    this.customerConsumerService = customerConsumerService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, version = V1)
  public ResponseEntity<ServiceResponse<CustomerDto>> createCustomer(
      @Valid @RequestBody CustomerConsumerCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(customerConsumerService.createCustomer(request));
  }

  @GetMapping(path = "/{customerId}", version = V1)
  public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(
      @PathVariable @Min(1) Integer customerId) {
    return ResponseEntity.ok(customerConsumerService.getCustomer(customerId));
  }

  @GetMapping(version = V1)
  public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(
      @ModelAttribute CustomerConsumerSearchCriteria criteria,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "5") @Min(1) @Max(10) int size,
      @RequestParam(defaultValue = "customerId") CustomerSortField sortBy,
      @RequestParam(defaultValue = "asc") SortDirection direction) {
    return ResponseEntity.ok(
        customerConsumerService.getCustomers(
            criteria.name(), criteria.email(), page, size, sortBy, direction));
  }

  @PutMapping(path = "/{customerId}", version = V1, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ServiceResponse<CustomerDto>> updateCustomer(
      @PathVariable @Min(1) Integer customerId,
      @Valid @RequestBody CustomerConsumerUpdateRequest request) {
    return ResponseEntity.ok(customerConsumerService.updateCustomer(customerId, request));
  }

  @DeleteMapping(path = "/{customerId}", version = V1)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> deleteCustomer(@PathVariable @Min(1) Integer customerId) {
    customerConsumerService.deleteCustomer(customerId);
    return ResponseEntity.noContent().build();
  }
}
