package io.github.blueprintplatform.samples.typecoverage.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
      "io.github.blueprintplatform.samples.typecoverage.consumer",
      "io.github.blueprintplatform.samples.typecoverage.client"
    })
public class ByoeTypeCoverageConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ByoeTypeCoverageConsumerApplication.class, args);
  }
}
