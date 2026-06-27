package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.OpenApiGenericsProperties;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.*;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver.ConfiguredContainerTypesResolver;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver.DefaultSupportedContainerTypesResolver;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver.SupportedContainerTypesResolver;
import io.github.blueprintplatform.openapi.generics.server.core.pipeline.OpenApiPipelineOrchestrator;
import io.github.blueprintplatform.openapi.generics.server.core.schema.ContractSchemaExclusionApplier;
import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaProcessor;
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

/**
 * Main auto-configuration for OpenAPI Generics server support.
 *
 * <p>Registers response type discovery, generic response introspection, contract guarding, and the
 * OpenAPI customization pipeline.
 */
@AutoConfiguration(after = OpenApiGenericsSchemaAutoConfiguration.class)
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
  public SupportedContainerTypesResolver supportedContainerTypesResolver() {
    return new DefaultSupportedContainerTypesResolver();
  }

  @Bean
  @ConditionalOnMissingBean
  public ConfiguredContainerTypesResolver configuredContainerTypesResolver() {
    return new ConfiguredContainerTypesResolver();
  }

  @Bean
  @ConditionalOnMissingBean
  public ResponseIntrospectionPolicyResolver responseIntrospectionPolicyResolver(
      SupportedContainerTypesResolver supportedContainerTypesResolver,
      ConfiguredContainerTypesResolver configuredContainerTypesResolver) {
    return new ResponseIntrospectionPolicyResolver(
        supportedContainerTypesResolver, configuredContainerTypesResolver);
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
  public ContractSchemaExclusionApplier schemaGenerationControlMarker() {
    return new ContractSchemaExclusionApplier();
  }

  @Bean
  @ConditionalOnMissingBean
  public OpenApiContractGuard openApiContractGuard() {
    return new OpenApiContractGuard();
  }

  @Bean
  @ConditionalOnMissingBean
  public OpenApiPipelineOrchestrator openApiPipelineOrchestrator(
      ContractSchemaExclusionApplier contractSchemaExclusionApplier,
      ResponseTypeDiscoveryStrategy discoveryStrategy,
      ResponseTypeIntrospector introspector,
      WrapperSchemaProcessor wrapperSchemaProcessor,
      OpenApiContractGuard contractGuard) {

    return new OpenApiPipelineOrchestrator(
        contractSchemaExclusionApplier,
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
