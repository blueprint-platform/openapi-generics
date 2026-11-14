package io.github.blueprintplatform.samples.gradle.customer.client.adapter;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;

/** Consumer-facing client library API; generated transport types stay behind this boundary. */
public interface CustomerClientAdapter {
  ServiceResponse<CustomerDto> getCustomer(long id);

  ServiceResponse<Page<CustomerDto>> getCustomers();
}
