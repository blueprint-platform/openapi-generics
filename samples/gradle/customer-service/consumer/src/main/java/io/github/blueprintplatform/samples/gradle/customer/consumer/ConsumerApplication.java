package io.github.blueprintplatform.samples.gradle.customer.consumer;

import io.github.blueprintplatform.samples.gradle.customer.generated.api.CustomersApi;
import io.github.blueprintplatform.samples.gradle.customer.generated.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class ConsumerApplication {
  public static void main(String[] args) {
    var context = SpringApplication.run(ConsumerApplication.class, args);
    var gateway = context.getBean(CustomerGateway.class);
    System.out.println(gateway.customer(1));
    System.out.println(gateway.customers());
    context.close();
  }

  @Bean
  CustomerGateway customerGateway(
      RestClient.Builder builder, @Value("${customer.base-url}") String baseUrl) {
    return new CustomerGateway(
        new CustomersApi(new ApiClient(builder.build()).setBasePath(baseUrl)));
  }
}
