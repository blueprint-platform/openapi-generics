package io.github.blueprintplatform.samples.gradle.customer.client.adapter.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import io.github.blueprintplatform.samples.gradle.customer.client.common.problem.ApiProblemException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

class CustomerApiClientConfigTest {
  @ParameterizedTest
  @ValueSource(strings = {"", "not-json", "{}"})
  void unreadableUpstreamErrorsRetainHttpStatusBehindStableException(String body) {
    var builder = RestClient.builder();
    var server = MockRestServiceServer.bindTo(builder).build();
    var adapter =
        new CustomerApiClientConfig()
            .customerClientAdapter(builder, JsonMapper.builder().build(), "http://producer");
    server
        .expect(requestTo("http://producer/customers/1"))
        .andRespond(
            withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.TEXT_PLAIN)
                .body(body));

    var exception = assertThrows(ApiProblemException.class, () -> adapter.getCustomer(1));
    assertThat(exception.getStatus()).isEqualTo(503);
    assertThat(exception.getTitle()).isEqualTo("Service Unavailable");
    assertThat(exception.getDetail()).isEqualTo("Customer service returned an HTTP error.");
    server.verify();
  }

  @Test
  void transportFailuresBecomeBadGatewayWithoutExposingTransportExceptions() {
    var builder = RestClient.builder();
    var server = MockRestServiceServer.bindTo(builder).build();
    var adapter =
        new CustomerApiClientConfig()
            .customerClientAdapter(builder, JsonMapper.builder().build(), "http://producer");
    server
        .expect(requestTo("http://producer/customers"))
        .andRespond(
            request -> {
              throw new IOException("connection failed");
            });

    var exception = assertThrows(ApiProblemException.class, adapter::getCustomers);
    assertThat(exception.getStatus()).isEqualTo(502);
    assertThat(exception.getTitle()).isEqualTo("Bad Gateway");
    assertThat(exception.getCause()).isNotNull();
    server.verify();
  }
}
