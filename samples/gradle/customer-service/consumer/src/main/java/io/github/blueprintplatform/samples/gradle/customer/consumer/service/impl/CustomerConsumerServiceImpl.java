package io.github.blueprintplatform.samples.gradle.customer.consumer.service.impl;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.consumer.service.CustomerConsumerService;
import io.github.blueprintplatform.samples.gradle.customer.consumer.service.client.CustomerServiceClient;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import org.springframework.stereotype.Service;

@Service
public class CustomerConsumerServiceImpl implements CustomerConsumerService {
  private final CustomerServiceClient client;

  public CustomerConsumerServiceImpl(CustomerServiceClient client) {
    this.client = client;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(long id) {
    return client.getCustomer(id);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers() {
    return client.getCustomers();
  }
}
