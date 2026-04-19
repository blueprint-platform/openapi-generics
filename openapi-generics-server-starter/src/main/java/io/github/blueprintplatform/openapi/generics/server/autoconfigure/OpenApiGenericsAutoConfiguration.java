package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.OpenApiGenericsProperties;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseIntrospectionPolicy;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseIntrospectionPolicyResolver;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeIntrospector;
import io.github.blueprintplatform.openapi.generics.server.core.pipeline.OpenApiPipelineOrchestrator;
import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaEnricher;
import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaProcessor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.control.SchemaGenerationControlMarker;
import io.github.blueprintplatform.openapi.generics.server.core.validation.OpenApiContractGuard;
import io.github.blueprintplatform.openapi.generics.server.mvc.MvcResponseTypeDiscoveryStrategy;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
@ConditionalOnClass(OpenApiCustomizer.class)
@ConditionalOnWebApplication
@EnableConfigurationProperties(OpenApiGenericsProperties.class)
public class OpenApiGenericsAutoConfiguration {

  @Bean
  @ConditionalOnClass(RequestMappingHandlerMapping.class)
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  @ConditionalOnMissingBean(ResponseTypeDiscoveryStrategy.class)
  public ResponseTypeDiscoveryStrategy mvcStrategy(ListableBeanFactory beanFactory) {
    return new MvcResponseTypeDiscoveryStrategy(beanFactory);
  }

  @Bean
  @ConditionalOnMissingBean
  public ResponseIntrospectionPolicyResolver responseIntrospectionPolicyResolver() {
    return new ResponseIntrospectionPolicyResolver();
  }

  @Bean
  @ConditionalOnMissingBean
  public ResponseIntrospectionPolicy responseIntrospectionPolicy(
      OpenApiGenericsProperties properties, ResponseIntrospectionPolicyResolver resolver) {
    return resolver.resolve(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public ResponseTypeIntrospector responseTypeIntrospector(ResponseIntrospectionPolicy policy) {
    return new ResponseTypeIntrospector(policy);
  }

  @Bean
  @ConditionalOnMissingBean
  public SchemaGenerationControlMarker schemaGenerationControlMarker() {
    return new SchemaGenerationControlMarker();
  }

  @Bean
  @ConditionalOnMissingBean
  public WrapperSchemaProcessor wrapperSchemaProcessor(WrapperSchemaEnricher enricher) {
    return new WrapperSchemaProcessor(enricher);
  }

  @Bean
  @ConditionalOnMissingBean
  public WrapperSchemaEnricher wrapperSchemaEnricher() {
    return new WrapperSchemaEnricher();
  }

  @Bean
  @ConditionalOnMissingBean
  public OpenApiContractGuard openApiContractGuard() {
    return new OpenApiContractGuard();
  }

  @Bean
  @ConditionalOnMissingBean
  public OpenApiPipelineOrchestrator openApiPipelineOrchestrator(
          SchemaGenerationControlMarker schemaGenerationControlMarker,
          ResponseTypeDiscoveryStrategy discoveryStrategy,
          ResponseTypeIntrospector introspector,
          WrapperSchemaProcessor wrapperSchemaProcessor,
          OpenApiContractGuard contractGuard) {

    return new OpenApiPipelineOrchestrator(
            schemaGenerationControlMarker,
            discoveryStrategy,
            introspector,
            wrapperSchemaProcessor,
            contractGuard);
  }

  @Bean
  @ConditionalOnMissingBean(name = "openApiGenericsCustomizer")
  public OpenApiCustomizer openApiGenericsCustomizer(OpenApiPipelineOrchestrator orchestrator) {
    return orchestrator::run;
  }
}