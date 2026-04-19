package io.github.blueprintplatform.samples.customerservice.consumer.service.client.impl;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerUpdateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.service.client.CustomerServiceClient;
import io.github.blueprintplatform.samples.customerservice.consumer.service.client.mapper.CustomerRequestMapper;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceClientImpl implements CustomerServiceClient {

  private final CustomerClientAdapter adapter;
  private final CustomerRequestMapper requestMapper;

  public CustomerServiceClientImpl(
      CustomerClientAdapter adapter,
      CustomerRequestMapper requestMapper) {
    this.adapter = adapter;
    this.requestMapper = requestMapper;
  }

  @Override
  public ServiceResponse<CustomerDto> createCustomer(
      CustomerConsumerCreateRequest customerConsumerCreateRequest) {
    return adapter.createCustomer(requestMapper.from(customerConsumerCreateRequest));
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer customerId) {
    return adapter.getCustomer(customerId);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers(
      String name,
      String email,
      Integer page,
      Integer size,
      CustomerSortField sortBy,
      SortDirection direction) {
    return adapter.getCustomers(name, email, page, size, sortBy, direction);
  }

  @Override
  public ServiceResponse<CustomerDto> updateCustomer(
      Integer customerId, CustomerConsumerUpdateRequest consumerUpdateRequest) {
    return adapter.updateCustomer(customerId, requestMapper.from(consumerUpdateRequest));
  }

  @Override
  public ServiceResponse<Void> deleteCustomer(Integer customerId) {
    return adapter.deleteCustomer(customerId);
  }
}
