package io.github.blueprintplatform.samples.customerservice.consumer.service;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerUpdateRequest;

/**
 * Orchestration layer for customer operations. Coordinates remote calls via CustomerServiceClient
 * and provides the integration point for cross-cutting concerns such as caching, fallback, or
 * response aggregation.
 */
public interface CustomerConsumerService {

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
