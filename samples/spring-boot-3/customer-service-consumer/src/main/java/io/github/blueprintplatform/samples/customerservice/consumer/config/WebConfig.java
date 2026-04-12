package io.github.blueprintplatform.samples.customerservice.consumer.config;

import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(String.class, CustomerSortField.class, CustomerSortField::from);
    registry.addConverter(String.class, SortDirection.class, SortDirection::from);
  }
}
