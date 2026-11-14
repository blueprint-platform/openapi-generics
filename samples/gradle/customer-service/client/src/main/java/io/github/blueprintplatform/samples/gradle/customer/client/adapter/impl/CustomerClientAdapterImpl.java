package io.github.blueprintplatform.samples.gradle.customer.client.adapter.impl;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import io.github.blueprintplatform.samples.gradle.customer.generated.api.CustomersApi;

public class CustomerClientAdapterImpl implements CustomerClientAdapter {
  private final CustomersApi api;

  public CustomerClientAdapterImpl(CustomersApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(long id) {
    return api.getCustomer(id);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers() {
    return api.listCustomers();
  }
}
