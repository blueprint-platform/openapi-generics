package consumer.service.client.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.customerservice.client.common.problem.ApiProblemException;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerUpdateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerUpdateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.common.exception.CustomerConsumerException;
import io.github.blueprintplatform.samples.customerservice.consumer.common.mapper.CustomerConsumerExceptionMapper;
import io.github.blueprintplatform.samples.customerservice.consumer.service.client.impl.CustomerServiceClientImpl;
import io.github.blueprintplatform.samples.customerservice.consumer.service.client.mapper.CustomerRequestMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: CustomerServiceClientImpl")
class CustomerServiceClientImplTest {

  private final CustomerClientAdapter adapter = mock(CustomerClientAdapter.class);
  private final CustomerConsumerExceptionMapper exceptionMapper =
      mock(CustomerConsumerExceptionMapper.class);
  private final CustomerRequestMapper requestMapper = mock(CustomerRequestMapper.class);

  private final CustomerServiceClientImpl service =
      new CustomerServiceClientImpl(adapter, exceptionMapper, requestMapper);

  @Test
  @DisplayName("createCustomer -> maps request and returns response")
  void createCustomer_success() {
    var consumerReq = new CustomerConsumerCreateRequest("John", "john@mail.com");
    var mappedReq = new CustomerCreateRequest().name("John").email("john@mail.com");

    var dto = new CustomerDto(1, "John", "john@mail.com");
    var response = ServiceResponse.of(dto);

    when(requestMapper.from(consumerReq)).thenReturn(mappedReq);
    when(adapter.createCustomer(mappedReq)).thenReturn(response);

    var result = service.createCustomer(consumerReq);

    assertNotNull(result);
    assertEquals(dto, result.getData());

    verify(requestMapper).from(consumerReq);
    verify(adapter).createCustomer(mappedReq);
  }

  @Test
  @DisplayName("createCustomer -> maps exception")
  void createCustomer_exception() {
    var consumerReq = new CustomerConsumerCreateRequest("John", "john@mail.com");
    var mappedReq = new CustomerCreateRequest();

    var apiEx = mock(ApiProblemException.class);
    var mappedEx = mock(CustomerConsumerException.class);

    when(requestMapper.from(consumerReq)).thenReturn(mappedReq);
    when(adapter.createCustomer(mappedReq)).thenThrow(apiEx);
    when(exceptionMapper.from(apiEx)).thenReturn(mappedEx);

    var thrown =
        assertThrows(CustomerConsumerException.class, () -> service.createCustomer(consumerReq));

    assertSame(mappedEx, thrown);
  }

  @Test
  @DisplayName("getCustomer -> success")
  void getCustomer_success() {
    var dto = new CustomerDto(1, "John", "john@mail.com");
    var response = ServiceResponse.of(dto);

    when(adapter.getCustomer(1)).thenReturn(response);

    var result = service.getCustomer(1);

    assertEquals(dto, result.getData());
    verify(adapter).getCustomer(1);
  }

  @Test
  @DisplayName("getCustomers -> success")
  void getCustomers_success() {
    var dto = new CustomerDto(1, "John", "john@mail.com");
    var page = Page.of(List.of(dto), 0, 5, 1);
    var response = ServiceResponse.of(page);

    when(adapter.getCustomers(any(), any(), any(), any(), any(), any())).thenReturn(response);

    var result =
        service.getCustomers("John", null, 0, 5, CustomerSortField.CUSTOMER_ID, SortDirection.ASC);

    assertNotNull(result);
    assertEquals(1, result.getData().content().size());
  }

  @Test
  @DisplayName("updateCustomer -> maps request")
  void updateCustomer_success() {
    var consumerReq = new CustomerConsumerUpdateRequest("Jane", "jane@mail.com");
    var mappedReq = new CustomerUpdateRequest().name("Jane").email("jane@mail.com");

    var dto = new CustomerDto(1, "Jane", "jane@mail.com");
    var response = ServiceResponse.of(dto);

    when(requestMapper.from(consumerReq)).thenReturn(mappedReq);
    when(adapter.updateCustomer(1, mappedReq)).thenReturn(response);

    var result = service.updateCustomer(1, consumerReq);

    assertEquals(dto, result.getData());

    verify(requestMapper).from(consumerReq);
    verify(adapter).updateCustomer(1, mappedReq);
  }

  @Test
  @DisplayName("deleteCustomer -> success")
  void deleteCustomer_success() {
    ServiceResponse<Void> response = ServiceResponse.of(null);

    when(adapter.deleteCustomer(1)).thenReturn(response);

    var result = service.deleteCustomer(1);

    assertNotNull(result);
    verify(adapter).deleteCustomer(1);
  }

  @Test
  @DisplayName("deleteCustomer -> exception mapped")
  void deleteCustomer_exception() {
    var apiEx = mock(ApiProblemException.class);
    var mappedEx = mock(CustomerConsumerException.class);

    when(adapter.deleteCustomer(1)).thenThrow(apiEx);
    when(exceptionMapper.from(apiEx)).thenReturn(mappedEx);

    var thrown = assertThrows(CustomerConsumerException.class, () -> service.deleteCustomer(1));

    assertSame(mappedEx, thrown);
  }
}
