package io.github.blueprintplatform.samples.customerservice.consumer.service.client;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerUpdateRequest;

/**
 * Consumer-side boundary for customer service remote calls. Isolates the consumer from the client
 * adapter, enabling independent testing and future evolution.
 */
public interface CustomerServiceClient {

  ServiceResponse<CustomerDto> createCustomer(CustomerConsumerCreateRequest request);

  ServiceResponse<CustomerDto> getCustomer(Integer customerId);

  ServiceResponse<Page<CustomerDto>> getCustomers(
      String name,
      String email,
      Integer page,
      Integer size,
      CustomerSortField sortBy,
      SortDirection direction);

  ServiceResponse<CustomerDto> updateCustomer(
      Integer customerId, CustomerConsumerUpdateRequest request);

  ServiceResponse<Void> deleteCustomer(Integer customerId);
}
