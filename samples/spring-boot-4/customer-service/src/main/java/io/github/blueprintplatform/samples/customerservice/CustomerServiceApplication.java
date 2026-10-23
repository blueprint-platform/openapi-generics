package io.github.blueprintplatform.samples.customerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "io.github.blueprintplatform.samples.customerservice")
public class CustomerServiceApplication {
  static void main(String[] args) {
    SpringApplication.run(CustomerServiceApplication.class, args);
  }
}
