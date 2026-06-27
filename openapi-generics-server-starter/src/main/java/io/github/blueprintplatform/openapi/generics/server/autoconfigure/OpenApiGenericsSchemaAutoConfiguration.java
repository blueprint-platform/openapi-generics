package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaProcessor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.enrichment.ContainerSchemaMetadataResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.enrichment.WrapperSchemaEnricher;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extraction.ArrayItemReferenceExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolution.ComponentContainerSchemaResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolution.WrapperPayloadArraySchemaResolver;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Schema auto-configuration for OpenAPI Generics server support.
 *
 * <p>Registers schema resolution and enrichment components used to process projected generic
 * wrapper schemas.
 */
@AutoConfiguration(before = OpenApiGenericsAutoConfiguration.class)
@ConditionalOnClass(OpenApiCustomizer.class)
@ConditionalOnWebApplication
public class OpenApiGenericsSchemaAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(ArrayItemReferenceExtractor.class)
  public ArrayItemReferenceExtractor arrayItemReferenceExtractor() {
    return new ArrayItemReferenceExtractor();
  }

  @Bean
  @ConditionalOnMissingBean(ComponentContainerSchemaResolver.class)
  public ComponentContainerSchemaResolver componentContainerSchemaResolver() {
    return new ComponentContainerSchemaResolver();
  }

  @Bean
  @ConditionalOnMissingBean(WrapperPayloadArraySchemaResolver.class)
  public WrapperPayloadArraySchemaResolver wrapperPayloadArraySchemaResolver() {
    return new WrapperPayloadArraySchemaResolver();
  }

  @Bean
  @ConditionalOnMissingBean
  public ContainerSchemaMetadataResolver containerSchemaMetadataResolver(
      WrapperPayloadArraySchemaResolver wrapperPayloadArraySchemaResolver,
      ComponentContainerSchemaResolver componentContainerSchemaResolver,
      ArrayItemReferenceExtractor arrayItemReferenceExtractor) {
    return new ContainerSchemaMetadataResolver(
        wrapperPayloadArraySchemaResolver,
        componentContainerSchemaResolver,
        arrayItemReferenceExtractor);
  }

  @Bean
  @ConditionalOnMissingBean
  public WrapperSchemaEnricher wrapperSchemaEnricher(
      ContainerSchemaMetadataResolver containerSchemaMetadataResolver) {
    return new WrapperSchemaEnricher(containerSchemaMetadataResolver);
  }

  @Bean
  @ConditionalOnMissingBean
  public WrapperSchemaProcessor wrapperSchemaProcessor(WrapperSchemaEnricher enricher) {
    return new WrapperSchemaProcessor(enricher);
  }
}
