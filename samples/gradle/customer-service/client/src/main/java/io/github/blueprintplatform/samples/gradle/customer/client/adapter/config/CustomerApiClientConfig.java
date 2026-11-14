package io.github.blueprintplatform.samples.gradle.customer.client.adapter.config;

import io.github.blueprintplatform.samples.gradle.customer.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.gradle.customer.client.adapter.impl.CustomerClientAdapterImpl;
import io.github.blueprintplatform.samples.gradle.customer.generated.api.CustomersApi;
import io.github.blueprintplatform.samples.gradle.customer.generated.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CustomerApiClientConfig {
  @Bean
  CustomerClientAdapter customerClientAdapter(
      RestClient.Builder builder, @Value("${customer.base-url}") String baseUrl) {
    var api = new CustomersApi(new ApiClient(builder.build()).setBasePath(baseUrl));
    return new CustomerClientAdapterImpl(api);
  }
}
