package io.github.blueprintplatform.samples.gradle.customer.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import io.github.blueprintplatform.samples.gradle.customer.producer.ProducerApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerHttpTest {
  private ConfigurableApplicationContext producer;
  private ConfigurableApplicationContext consumer;
  private RestClient http;

  @BeforeAll
  void startApplications() {
    producer =
        new SpringApplicationBuilder(ProducerApplication.class)
            .web(WebApplicationType.SERVLET)
            .run("--server.port=0");
    consumer =
        new SpringApplicationBuilder(ConsumerApplication.class)
            .web(WebApplicationType.SERVLET)
            .run("--server.port=0", "--customer.base-url=" + baseUrl(producer));
    http = consumer.getBean(RestClient.Builder.class).baseUrl(baseUrl(consumer)).build();
  }

  @AfterAll
  void stopApplications() {
    if (consumer != null) {
      consumer.close();
    }
    if (producer != null) {
      producer.close();
    }
  }

  @Test
  void consumerEndpointsPreserveDirectAndPagedContractsThroughBothHttpHops() {
    ServiceResponse<CustomerDto> direct =
        http.get().uri("/customers/1").retrieve().body(new ParameterizedTypeReference<>() {});
    assertThat(direct.getData())
        .isExactlyInstanceOf(CustomerDto.class)
        .isEqualTo(new CustomerDto(1, "Ada"));
    assertThat(direct.getMeta().serverTime()).isNotNull();

    ServiceResponse<Page<CustomerDto>> paged =
        http.get().uri("/customers").retrieve().body(new ParameterizedTypeReference<>() {});
    assertThat(paged.getData()).isExactlyInstanceOf(Page.class);
    assertThat(paged.getData().content()).containsExactly(new CustomerDto(1, "Ada"));
    assertThat(paged.getData().content().getFirst()).isExactlyInstanceOf(CustomerDto.class);
    assertThat(paged.getData().totalElements()).isEqualTo(1);
    assertThat(paged.getData().totalPages()).isEqualTo(1);
    assertThat(paged.getData().hasNext()).isFalse();
    assertThat(paged.getMeta().serverTime()).isNotNull();
  }

  @Test
  void adapterRetainsGeneratedWrapperDeserializationBehindItsStableInterface() {
    var adapter = consumer.getBean(CustomerClientAdapter.class);
    var direct = adapter.getCustomer(1);
    assertThat(direct.getClass().getSuperclass()).isEqualTo(ServiceResponse.class);
    assertThat(direct.getData()).isExactlyInstanceOf(CustomerDto.class);
    var paged = adapter.getCustomers();
    assertThat(paged.getClass().getSuperclass()).isEqualTo(ServiceResponse.class);
    assertThat(paged.getData()).isExactlyInstanceOf(Page.class);
    assertThat(paged.getData().content().getFirst()).isExactlyInstanceOf(CustomerDto.class);
  }

  @Test
  void upstreamNotFoundBecomesConsumerProblemResponse() {
    var response = http.get().uri("/customers/999").retrieve();
    var error = assertThrows(RestClientResponseException.class, response::toBodilessEntity);
    assertThat(error.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(error.getResponseHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    var problem = error.getResponseBodyAs(ProblemDetail.class);
    assertThat(problem.getStatus()).isEqualTo(404);
    assertThat(problem.getTitle()).isEqualTo("Not Found");
    assertThat(problem.getDetail()).isEqualTo("Customer 999 not found");
    assertThat(problem.getInstance().getPath()).isEqualTo("/customers/999");
  }

  @Test
  void invalidConsumerPathReturnsBadRequestProblem() {
    var response = http.get().uri("/customers/invalid").retrieve();
    var error = assertThrows(RestClientResponseException.class, response::toBodilessEntity);
    assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(error.getResponseBodyAs(ProblemDetail.class).getStatus()).isEqualTo(400);
  }

  private static String baseUrl(ConfigurableApplicationContext context) {
    return "http://localhost:" + context.getEnvironment().getRequiredProperty("local.server.port");
  }
}
