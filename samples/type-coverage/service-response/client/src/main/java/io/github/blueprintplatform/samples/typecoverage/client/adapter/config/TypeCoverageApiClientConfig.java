package io.github.blueprintplatform.samples.typecoverage.client.adapter.config;

import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ObjectPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.PagedPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ScalarPayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.ValuePayloadControllerApi;
import io.github.blueprintplatform.samples.typecoverage.client.generated.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TypeCoverageApiClientConfig {

  @Bean
  RestClient typeCoverageRestClient(RestClient.Builder builder) {
    return builder.build();
  }

  @Bean
  ApiClient typeCoverageApiClient(
      RestClient typeCoverageRestClient, @Value("${type-coverage.api.base-url}") String baseUrl) {
    return new ApiClient(typeCoverageRestClient).setBasePath(baseUrl);
  }

  @Bean
  ScalarPayloadControllerApi scalarPayloadControllerApi(ApiClient typeCoverageApiClient) {
    return new ScalarPayloadControllerApi(typeCoverageApiClient);
  }

  @Bean
  ValuePayloadControllerApi valuePayloadControllerApi(ApiClient typeCoverageApiClient) {
    return new ValuePayloadControllerApi(typeCoverageApiClient);
  }

  @Bean
  ObjectPayloadControllerApi objectPayloadControllerApi(ApiClient typeCoverageApiClient) {
    return new ObjectPayloadControllerApi(typeCoverageApiClient);
  }

  @Bean
  PagedPayloadControllerApi pagedPayloadControllerApi(ApiClient typeCoverageApiClient) {
    return new PagedPayloadControllerApi(typeCoverageApiClient);
  }
}
