package io.github.blueprintplatform.samples.customerservice.consumer.service.client.mapper;

import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerUpdateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.consumer.api.dto.CustomerConsumerUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomerRequestMapper {

  public CustomerCreateRequest from(CustomerConsumerCreateRequest source) {
    if (source == null) {
      return null;
    }

    return new CustomerCreateRequest().name(source.name()).email(source.email());
  }

  public CustomerUpdateRequest from(CustomerConsumerUpdateRequest source) {
    if (source == null) {
      return null;
    }

    return new CustomerUpdateRequest().name(source.name()).email(source.email());
  }
}
