package io.github.blueprintplatform.samples.gradle.customer.client.adapter.impl;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.gradle.customer.client.common.problem.ApiProblemException;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import io.github.blueprintplatform.samples.gradle.customer.generated.api.CustomersApi;
import java.util.function.Supplier;
import org.springframework.web.client.RestClientException;

public class CustomerClientAdapterImpl implements CustomerClientAdapter {
  private final CustomersApi api;

  public CustomerClientAdapterImpl(CustomersApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(long id) {
    return execute(() -> api.getCustomer(id));
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers() {
    return execute(api::listCustomers);
  }

  private static <T> ServiceResponse<T> execute(Supplier<? extends ServiceResponse<T>> action) {
    try {
      return action.get();
    } catch (RestClientException exception) {
      throw new ApiProblemException(
          502, "Bad Gateway", "Customer service response could not be retrieved.", exception);
    }
  }
}
