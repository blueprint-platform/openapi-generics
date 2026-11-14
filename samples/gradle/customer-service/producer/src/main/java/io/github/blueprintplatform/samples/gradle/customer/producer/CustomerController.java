package io.github.blueprintplatform.samples.gradle.customer.producer;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import io.github.blueprintplatform.samples.gradle.customer.producer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Customers")
public class CustomerController {
  private final CustomerService service;

  public CustomerController(CustomerService service) {
    this.service = service;
  }

  @GetMapping("/customers/{id}")
  @Operation(operationId = "getCustomer")
  @ApiResponse(responseCode = "200", description = "Customer found")
  @ApiResponse(
      responseCode = "404",
      description = "Customer not found",
      content =
          @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class)))
  public ServiceResponse<CustomerDto> getCustomer(@PathVariable("id") long id) {
    return ServiceResponse.of(service.getCustomer(id));
  }

  @GetMapping("/customers")
  @Operation(operationId = "listCustomers")
  public ServiceResponse<Page<CustomerDto>> listCustomers() {
    return ServiceResponse.of(service.getCustomers());
  }
}
