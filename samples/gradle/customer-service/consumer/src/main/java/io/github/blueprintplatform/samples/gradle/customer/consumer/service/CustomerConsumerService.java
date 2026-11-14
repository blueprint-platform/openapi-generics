package io.github.blueprintplatform.samples.gradle.customer.consumer.service;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;

public interface CustomerConsumerService {
  ServiceResponse<CustomerDto> getCustomer(long id);

  ServiceResponse<Page<CustomerDto>> getCustomers();
}
