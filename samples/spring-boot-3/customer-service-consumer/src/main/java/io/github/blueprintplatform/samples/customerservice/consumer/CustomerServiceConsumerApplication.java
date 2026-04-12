package io.github.blueprintplatform.samples.customerservice.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "io.github.blueprintplatform.samples.customerservice.consumer",
        "io.github.blueprintplatform.samples.customerservice.client"
})
public class CustomerServiceConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceConsumerApplication.class, args);
    }
}