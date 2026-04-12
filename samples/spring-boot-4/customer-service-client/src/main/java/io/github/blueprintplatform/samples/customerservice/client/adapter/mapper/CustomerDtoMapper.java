package io.github.blueprintplatform.samples.customerservice.client.adapter.mapper;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import org.springframework.stereotype.Component;

/**
 * Maps generated CustomerDto to the shared contract CustomerDto.
 * Isolates the adapter from generated model internals.
 */
@Component
public class CustomerDtoMapper {

    public CustomerDto toContract(
            io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerDto generated) {
        if (generated == null) {
            return null;
        }
        return new CustomerDto(
                generated.getCustomerId(),
                generated.getName(),
                generated.getEmail());
    }
}