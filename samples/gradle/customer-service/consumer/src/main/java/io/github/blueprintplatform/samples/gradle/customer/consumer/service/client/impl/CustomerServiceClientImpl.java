package io.github.blueprintplatform.samples.gradle.customer.consumer.service.client.impl;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.gradle.customer.consumer.service.client.CustomerServiceClient;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceClientImpl implements CustomerServiceClient {
  private final CustomerClientAdapter adapter;

  public CustomerServiceClientImpl(CustomerClientAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(long id) {
    return adapter.getCustomer(id);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers() {
    return adapter.getCustomers();
  }
}
