package io.github.blueprintplatform.samples.gradle.customer.producer;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Customers")
public class CustomerController {
  @GetMapping("/customers/{id}")
  @Operation(operationId = "getCustomer")
  public ServiceResponse<CustomerDto> getCustomer(@PathVariable("id") long id) {
    return ServiceResponse.of(new CustomerDto(id, "Ada"));
  }

  @GetMapping("/customers")
  @Operation(operationId = "listCustomers")
  public ServiceResponse<Page<CustomerDto>> listCustomers() {
    return ServiceResponse.of(Page.of(List.of(new CustomerDto(1, "Ada")), 0, 10, 1));
  }
}
