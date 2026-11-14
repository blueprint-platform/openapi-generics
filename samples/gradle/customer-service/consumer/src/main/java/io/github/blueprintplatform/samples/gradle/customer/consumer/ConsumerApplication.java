package io.github.blueprintplatform.samples.gradle.customer.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
      "io.github.blueprintplatform.samples.gradle.customer.consumer",
      "io.github.blueprintplatform.samples.gradle.customer.client"
    })
public class ConsumerApplication {
  public static void main(String[] args) {
    SpringApplication.run(ConsumerApplication.class, args);
  }
}
