package io.github.blueprintplatform.samples.gradle.customer.consumer.service.client;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;

/** Consumer-owned service boundary for remote customer calls. */
public interface CustomerServiceClient {
  ServiceResponse<CustomerDto> getCustomer(long id);

  ServiceResponse<Page<CustomerDto>> getCustomers();
}
