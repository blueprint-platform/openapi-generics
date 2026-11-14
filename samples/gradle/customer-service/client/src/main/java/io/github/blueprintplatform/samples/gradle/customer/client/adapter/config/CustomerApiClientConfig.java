package io.github.blueprintplatform.samples.gradle.customer.client.adapter.config;

import io.github.blueprintplatform.samples.gradle.customer.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.gradle.customer.client.adapter.impl.CustomerClientAdapterImpl;
import io.github.blueprintplatform.samples.gradle.customer.client.common.problem.ApiProblemException;
import io.github.blueprintplatform.samples.gradle.customer.generated.api.CustomersApi;
import io.github.blueprintplatform.samples.gradle.customer.generated.invoker.ApiClient;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class CustomerApiClientConfig {
  @Bean
  CustomerClientAdapter customerClientAdapter(
      RestClient.Builder builder,
      ObjectMapper mapper,
      @Value("${customer.base-url}") String baseUrl) {
    var restClient =
        builder
            .clone()
            .defaultStatusHandler(
                HttpStatusCode::isError,
                (request, response) -> {
                  var problem = readProblem(mapper, response);
                  throw new ApiProblemException(
                      response.getStatusCode().value(), problem.getTitle(), problem.getDetail());
                })
            .build();
    var api = new CustomersApi(new ApiClient(restClient).setBasePath(baseUrl));
    return new CustomerClientAdapterImpl(api);
  }

  private static ProblemDetail readProblem(ObjectMapper mapper, ClientHttpResponse response)
      throws IOException {
    var status = response.getStatusCode();
    try {
      var problem = mapper.readValue(response.getBody(), ProblemDetail.class);
      if (problem != null && problem.getTitle() != null && problem.getDetail() != null) {
        return problem;
      }
    } catch (JacksonException | IOException exception) {
      // Empty, malformed or non-JSON upstream bodies still have a stable client error boundary.
    }
    return ProblemDetail.forStatusAndDetail(status, "Customer service returned an HTTP error.");
  }
}
