package io.github.blueprintplatform.samples.gradle.customer.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import io.github.blueprintplatform.samples.gradle.customer.producer.ProducerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

class CustomerHttpTest {
  @Test
  void generatedRestClientDeserializesDirectAndPagedContractsOverHttp() {
    try (var producer =
            new SpringApplicationBuilder(ProducerApplication.class)
                .web(WebApplicationType.SERVLET)
                .run("--server.port=0", "--spring.main.web-application-type=servlet");
        var consumer =
            new SpringApplicationBuilder(ConsumerApplication.class)
                .web(WebApplicationType.NONE)
                .run(
                    "--customer.base-url=http://localhost:"
                        + producer.getEnvironment().getRequiredProperty("local.server.port"))) {
      var gateway = consumer.getBean(CustomerGateway.class);
      ServiceResponse<CustomerDto> direct = gateway.customer(42);
      assertThat(direct.getClass().getSuperclass()).isEqualTo(ServiceResponse.class);
      assertThat(direct.getData())
          .isExactlyInstanceOf(CustomerDto.class)
          .isEqualTo(new CustomerDto(42, "Ada"));
      assertThat(direct.getMeta().serverTime()).isNotNull();
      ServiceResponse<Page<CustomerDto>> paged = gateway.customers();
      assertThat(paged.getClass().getSuperclass()).isEqualTo(ServiceResponse.class);
      assertThat(paged.getData()).isExactlyInstanceOf(Page.class);
      assertThat(paged.getData().content()).containsExactly(new CustomerDto(1, "Ada"));
      assertThat(paged.getData().content().getFirst()).isExactlyInstanceOf(CustomerDto.class);
      assertThat(paged.getData().totalElements()).isEqualTo(1);
      assertThat(paged.getData().totalPages()).isEqualTo(1);
      assertThat(paged.getData().hasNext()).isFalse();
    }
  }
}
