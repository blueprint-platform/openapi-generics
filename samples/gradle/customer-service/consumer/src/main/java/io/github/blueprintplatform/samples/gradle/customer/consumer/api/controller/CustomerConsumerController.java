package io.github.blueprintplatform.samples.gradle.customer.consumer.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.consumer.service.CustomerConsumerService;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/customers", produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerConsumerController {
  private final CustomerConsumerService service;

  public CustomerConsumerController(CustomerConsumerService service) {
    this.service = service;
  }

  @GetMapping("/{id}")
  public ServiceResponse<CustomerDto> getCustomer(@PathVariable("id") long id) {
    return service.getCustomer(id);
  }

  @GetMapping
  public ServiceResponse<Page<CustomerDto>> getCustomers() {
    return service.getCustomers();
  }
}
