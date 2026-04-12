package io.github.blueprintplatform.samples.customerservice.consumer.service.client.impl;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.customerservice.client.common.problem.ApiProblemException;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerUpdateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.common.mapper.CustomerConsumerExceptionMapper;
import io.github.blueprintplatform.samples.customerservice.consumer.service.client.CustomerServiceClient;
import io.github.blueprintplatform.samples.customerservice.consumer.service.client.mapper.CustomerRequestMapper;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceClientImpl implements CustomerServiceClient {

  private final CustomerClientAdapter adapter;
  private final CustomerConsumerExceptionMapper exceptionMapper;
  private final CustomerRequestMapper requestMapper;

  public CustomerServiceClientImpl(
      CustomerClientAdapter adapter,
      CustomerConsumerExceptionMapper exceptionMapper,
      CustomerRequestMapper requestMapper) {
    this.adapter = adapter;
    this.exceptionMapper = exceptionMapper;
    this.requestMapper = requestMapper;
  }

  @Override
  public ServiceResponse<CustomerDto> createCustomer(
      CustomerConsumerCreateRequest customerConsumerCreateRequest) {
    try {
      return adapter.createCustomer(requestMapper.from(customerConsumerCreateRequest));
    } catch (ApiProblemException ex) {
      throw exceptionMapper.from(ex);
    }
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer customerId) {
    try {
      return adapter.getCustomer(customerId);
    } catch (ApiProblemException ex) {
      throw exceptionMapper.from(ex);
    }
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers(
      String name,
      String email,
      Integer page,
      Integer size,
      CustomerSortField sortBy,
      SortDirection direction) {
    try {
      return adapter.getCustomers(name, email, page, size, sortBy, direction);
    } catch (ApiProblemException ex) {
      throw exceptionMapper.from(ex);
    }
  }

  @Override
  public ServiceResponse<CustomerDto> updateCustomer(
      Integer customerId, CustomerConsumerUpdateRequest consumerUpdateRequest) {
    try {
      return adapter.updateCustomer(customerId, requestMapper.from(consumerUpdateRequest));
    } catch (ApiProblemException ex) {
      throw exceptionMapper.from(ex);
    }
  }

  @Override
  public ServiceResponse<Void> deleteCustomer(Integer customerId) {
    try {
      return adapter.deleteCustomer(customerId);
    } catch (ApiProblemException ex) {
      throw exceptionMapper.from(ex);
    }
  }
}
