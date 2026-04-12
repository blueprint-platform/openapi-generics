package io.github.blueprintplatform.samples.customerservice.consumer.service.impl;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerUpdateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.service.CustomerConsumerService;
import io.github.blueprintplatform.samples.customerservice.consumer.service.client.CustomerServiceClient;
import org.springframework.stereotype.Service;

@Service
public class CustomerConsumerServiceImpl implements CustomerConsumerService {

  private final CustomerServiceClient customerServiceClient;

  public CustomerConsumerServiceImpl(CustomerServiceClient customerServiceClient) {
    this.customerServiceClient = customerServiceClient;
  }

  @Override
  public ServiceResponse<CustomerDto> createCustomer(CustomerConsumerCreateRequest request) {
    return customerServiceClient.createCustomer(request);
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer customerId) {
    return customerServiceClient.getCustomer(customerId);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers(
      String name,
      String email,
      Integer page,
      Integer size,
      CustomerSortField sortBy,
      SortDirection direction) {
    return customerServiceClient.getCustomers(name, email, page, size, sortBy, direction);
  }

  @Override
  public ServiceResponse<CustomerDto> updateCustomer(
      Integer customerId, CustomerConsumerUpdateRequest request) {
    return customerServiceClient.updateCustomer(customerId, request);
  }

  @Override
  public ServiceResponse<Void> deleteCustomer(Integer customerId) {
    return customerServiceClient.deleteCustomer(customerId);
  }
}
