package io.github.blueprintplatform.samples.gradle.customer.producer.service;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {
  private final List<CustomerDto> customers = List.of(new CustomerDto(1, "Ada"));

  public CustomerDto getCustomer(long id) {
    return customers.stream()
        .filter(customer -> customer.id() == id)
        .findFirst()
        .orElseThrow(
            () ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer " + id + " not found"));
  }

  public Page<CustomerDto> getCustomers() {
    return Page.of(customers, 0, 10, customers.size());
  }
}
