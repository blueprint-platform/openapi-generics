package io.github.blueprintplatform.samples.customerservice.client.adapter.impl;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.customerservice.client.adapter.mapper.CustomerDtoMapper;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.client.generated.api.CustomerControllerApi;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerUpdateRequest;
import org.springframework.stereotype.Service;

@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

  private final CustomerControllerApi api;
  private final CustomerDtoMapper mapper;

  public CustomerClientAdapterImpl(
      CustomerControllerApi customerControllerApi, CustomerDtoMapper mapper) {
    this.api = customerControllerApi;
    this.mapper = mapper;
  }

  @Override
  public ServiceResponse<CustomerDto> createCustomer(CustomerCreateRequest request) {
    var response = api.createCustomer(request);
    return mapData(response);
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer customerId) {
    var response = api.getCustomer(customerId);
    return mapData(response);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers() {
    return getCustomers(null, null, 0, 5, CustomerSortField.CUSTOMER_ID, SortDirection.ASC);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers(
      String name,
      String email,
      Integer page,
      Integer size,
      CustomerSortField sortBy,
      SortDirection direction) {
    var response =
        api.getCustomers(
            name,
            email,
            page,
            size,
            sortBy != null ? sortBy.value() : CustomerSortField.CUSTOMER_ID.value(),
            direction != null ? direction.value() : SortDirection.ASC.value());
    return mapPageData(response);
  }

  @Override
  public ServiceResponse<CustomerDto> updateCustomer(
      Integer customerId, CustomerUpdateRequest request) {
    var response = api.updateCustomer(customerId, request);
    return mapData(response);
  }

  @Override
  public ServiceResponse<Void> deleteCustomer(Integer customerId) {
    api.deleteCustomer(customerId);
    return ServiceResponse.of(null);
  }

  private ServiceResponse<CustomerDto> mapData(
      ServiceResponse<
              io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerDto>
          response) {
    CustomerDto mapped = mapper.toContract(response.getData());
    return ServiceResponse.of(mapped, response.getMeta());
  }

  private ServiceResponse<Page<CustomerDto>> mapPageData(
      ServiceResponse<
              Page<
                  io.github.blueprintplatform.samples.customerservice.client.generated.dto
                      .CustomerDto>>
          response) {
    Page<io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerDto>
        generatedPage = response.getData();
    if (generatedPage == null) {
      return ServiceResponse.of(null, response.getMeta());
    }
    var mappedContent = generatedPage.content().stream().map(mapper::toContract).toList();
    Page<CustomerDto> mappedPage =
        Page.of(
            mappedContent,
            generatedPage.page(),
            generatedPage.size(),
            generatedPage.totalElements());
    return ServiceResponse.of(mappedPage, response.getMeta());
  }
}
