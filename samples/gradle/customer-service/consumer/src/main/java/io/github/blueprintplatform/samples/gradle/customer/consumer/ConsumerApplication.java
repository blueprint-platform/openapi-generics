package io.github.blueprintplatform.samples.gradle.customer.consumer;

import io.github.blueprintplatform.samples.gradle.customer.consumer.service.client.CustomerServiceClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
      "io.github.blueprintplatform.samples.gradle.customer.consumer",
      "io.github.blueprintplatform.samples.gradle.customer.client"
    })
public class ConsumerApplication {
  public static void main(String[] args) {
    var context = SpringApplication.run(ConsumerApplication.class, args);
    var client = context.getBean(CustomerServiceClient.class);
    System.out.println(client.getCustomer(1));
    System.out.println(client.getCustomers());
    context.close();
  }
}
