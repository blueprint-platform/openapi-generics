package io.github.blueprintplatform.samples.customerservice.consumer.config;

import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.consumer.config.version.ApiOnlyVersionResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private static final String API_VERSION_HEADER = "API-Version";

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Value("${app.api.base-path:/customers}")
  private String apiBasePath;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(String.class, CustomerSortField.class, CustomerSortField::from);
    registry.addConverter(String.class, SortDirection.class, SortDirection::from);
  }

  @Override
  public void configureApiVersioning(ApiVersionConfigurer configurer) {
    configurer
        .useVersionResolver(
            new ApiOnlyVersionResolver(API_VERSION_HEADER, contextPath, apiBasePath))
        .setVersionRequired(false);
  }
}
