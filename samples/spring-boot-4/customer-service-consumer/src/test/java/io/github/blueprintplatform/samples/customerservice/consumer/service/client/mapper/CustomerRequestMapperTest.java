package io.github.blueprintplatform.samples.customerservice.consumer.service.client.mapper;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerUpdateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: CustomerRequestMapper")
class CustomerRequestMapperTest {

  private final CustomerRequestMapper mapper = new CustomerRequestMapper();

  @Test
  @DisplayName("from(CustomerConsumerCreateRequest) -> maps fields correctly")
  void shouldMapCreateRequest() {
    var source = new CustomerConsumerCreateRequest("John", "john@example.com");

    CustomerCreateRequest result = mapper.from(source);

    assertNotNull(result);
    assertEquals("John", result.getName());
    assertEquals("john@example.com", result.getEmail());
  }

  @Test
  @DisplayName("from(CustomerConsumerUpdateRequest) -> maps fields correctly")
  void shouldMapUpdateRequest() {
    var source = new CustomerConsumerUpdateRequest("Jane", "jane@example.com");

    CustomerUpdateRequest result = mapper.from(source);

    assertNotNull(result);
    assertEquals("Jane", result.getName());
    assertEquals("jane@example.com", result.getEmail());
  }

  @Test
  @DisplayName("from(null) -> returns null")
  void shouldReturnNullWhenSourceIsNull() {
    assertNull(mapper.from((CustomerConsumerCreateRequest) null));
    assertNull(mapper.from((CustomerConsumerUpdateRequest) null));
  }
}
