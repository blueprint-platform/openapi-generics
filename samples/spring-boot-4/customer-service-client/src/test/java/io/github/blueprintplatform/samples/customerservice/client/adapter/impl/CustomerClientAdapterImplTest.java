package io.github.blueprintplatform.samples.customerservice.client.adapter.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.customerservice.client.adapter.mapper.CustomerDtoMapper;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.client.generated.api.CustomerControllerApi;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerUpdateRequest;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.ServiceResponseCustomerDto;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.ServiceResponsePageCustomerDto;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: CustomerClientAdapterImpl")
class CustomerClientAdapterImplTest {

  @Mock CustomerControllerApi api;

  @Mock CustomerDtoMapper mapper;

  @InjectMocks CustomerClientAdapterImpl adapter;

  @Test
  @DisplayName("createCustomer -> delegates to API and maps generated dto to contract dto")
  void createCustomer_delegates_and_returns_data_meta() {
    var req = new CustomerCreateRequest().name("Jane Doe").email("jane@example.com");

    var generatedDto =
            new io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerDto()
                    .customerId(1)
                    .name("Jane Doe")
                    .email("jane@example.com");

    var contractDto = new CustomerDto(1, "Jane Doe", "jane@example.com");

    var serverOdt = OffsetDateTime.parse("2025-01-01T12:34:56Z");
    var meta = new Meta(serverOdt.toInstant(), List.of());

    var wrapper = new ServiceResponseCustomerDto();
    wrapper.setData(generatedDto);
    wrapper.setMeta(meta);

    when(api.createCustomer(any(CustomerCreateRequest.class))).thenReturn(wrapper);
    when(mapper.toContract(generatedDto)).thenReturn(contractDto);

    ServiceResponse<CustomerDto> res = adapter.createCustomer(req);

    assertNotNull(res);
    assertNotNull(res.getData());
    assertEquals(1, res.getData().customerId());
    assertEquals("Jane Doe", res.getData().name());
    assertEquals("jane@example.com", res.getData().email());

    assertNotNull(res.getMeta());
    assertEquals(serverOdt.toInstant(), res.getMeta().serverTime());
  }

  @Test
  @DisplayName("getCustomer -> delegates to API and maps generated dto to contract dto")
  void getCustomer_delegates_and_returnsDto() {
    var generatedDto =
            new io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerDto()
                    .customerId(42)
                    .name("John Smith")
                    .email("john.smith@example.com");

    var contractDto = new CustomerDto(42, "John Smith", "john.smith@example.com");

    var serverOdt = OffsetDateTime.parse("2025-02-01T10:00:00Z");
    var wrapper = new ServiceResponseCustomerDto();
    wrapper.setData(generatedDto);
    wrapper.setMeta(new Meta(serverOdt.toInstant(), List.of()));

    when(api.getCustomer(any())).thenReturn(wrapper);
    when(mapper.toContract(generatedDto)).thenReturn(contractDto);

    ServiceResponse<CustomerDto> res = adapter.getCustomer(42);

    assertNotNull(res);
    assertNotNull(res.getData());
    assertEquals(42, res.getData().customerId());
    assertEquals("John Smith", res.getData().name());
    assertEquals("john.smith@example.com", res.getData().email());

    assertNotNull(res.getMeta());
    assertEquals(serverOdt.toInstant(), res.getMeta().serverTime());
  }

  @Test
  @DisplayName("getCustomers -> delegates to API and maps generated page content to contract page")
  void getCustomers_delegates_and_returnsPage() {
    var generated1 =
            new io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerDto()
                    .customerId(1)
                    .name("A")
                    .email("a@example.com");

    var generated2 =
            new io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerDto()
                    .customerId(2)
                    .name("B")
                    .email("b@example.com");

    var contract1 = new CustomerDto(1, "A", "a@example.com");
    var contract2 = new CustomerDto(2, "B", "b@example.com");

    var page = Page.of(List.of(generated1, generated2), 0, 5, 2L);

    var serverOdt = OffsetDateTime.parse("2025-03-01T09:00:00Z");
    var wrapper = new ServiceResponsePageCustomerDto();
    wrapper.setData(page);
    wrapper.setMeta(new Meta(serverOdt.toInstant(), List.of()));

    when(api.getCustomers(any(), any(), any(), any(), any(), any())).thenReturn(wrapper);
    when(mapper.toContract(generated1)).thenReturn(contract1);
    when(mapper.toContract(generated2)).thenReturn(contract2);

    ServiceResponse<Page<CustomerDto>> res =
            adapter.getCustomers(null, null, 0, 5, CustomerSortField.CUSTOMER_ID, SortDirection.ASC);

    assertNotNull(res);
    assertNotNull(res.getData());
    assertEquals(0, res.getData().page());
    assertEquals(5, res.getData().size());
    assertEquals(2L, res.getData().totalElements());
    assertNotNull(res.getData().content());
    assertEquals(2, res.getData().content().size());
    assertEquals(1, res.getData().content().getFirst().customerId());
    assertEquals("A", res.getData().content().getFirst().name());
    assertEquals(2, res.getData().content().get(1).customerId());

    assertNotNull(res.getMeta());
    assertEquals(serverOdt.toInstant(), res.getMeta().serverTime());
  }

  @Test
  @DisplayName("updateCustomer -> delegates to API and maps generated dto to contract dto")
  void updateCustomer_delegates_and_returnsUpdated() {
    var req = new CustomerUpdateRequest().name("Jane Updated").email("jane.updated@example.com");

    var generatedDto =
            new io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerDto()
                    .customerId(1)
                    .name("Jane Updated")
                    .email("jane.updated@example.com");

    var contractDto = new CustomerDto(1, "Jane Updated", "jane.updated@example.com");

    var serverOdt = OffsetDateTime.parse("2025-04-02T12:00:00Z");
    var wrapper = new ServiceResponseCustomerDto();
    wrapper.setData(generatedDto);
    wrapper.setMeta(new Meta(serverOdt.toInstant(), List.of()));

    when(api.updateCustomer(any(), any(CustomerUpdateRequest.class))).thenReturn(wrapper);
    when(mapper.toContract(generatedDto)).thenReturn(contractDto);

    ServiceResponse<CustomerDto> res = adapter.updateCustomer(1, req);

    assertNotNull(res);
    assertNotNull(res.getData());
    assertEquals("Jane Updated", res.getData().name());
    assertEquals("jane.updated@example.com", res.getData().email());

    assertNotNull(res.getMeta());
    assertEquals(serverOdt.toInstant(), res.getMeta().serverTime());
  }

  @Test
  @DisplayName("deleteCustomer -> returns empty ServiceResponse")
  void deleteCustomer_delegates_and_wrapsVoidResponse() {
    doNothing().when(api).deleteCustomer(any());

    ServiceResponse<Void> res = adapter.deleteCustomer(7);

    assertNotNull(res);
    assertNull(res.getData());
    assertNotNull(res.getMeta());
  }

  @Test
  @DisplayName("Adapter interface type check")
  void adapter_type_sanity() {
    CustomerClientAdapter asInterface = adapter;
    assertNotNull(asInterface);
  }
}