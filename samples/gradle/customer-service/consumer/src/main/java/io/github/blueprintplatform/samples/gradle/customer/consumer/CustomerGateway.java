package io.github.blueprintplatform.samples.gradle.customer.consumer;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import io.github.blueprintplatform.samples.gradle.customer.generated.api.CustomersApi;

public class CustomerGateway {
  private final CustomersApi api;

  public CustomerGateway(CustomersApi api) {
    this.api = api;
  }

  public ServiceResponse<CustomerDto> customer(long id) {
    return api.getCustomer(id);
  }

  public ServiceResponse<Page<CustomerDto>> customers() {
    return api.listCustomers();
  }
}
