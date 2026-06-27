package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaProcessor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.enrichment.ContainerSchemaMetadataResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.enrichment.WrapperSchemaEnricher;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extraction.ArrayItemReferenceExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolution.ComponentContainerSchemaResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolution.WrapperPayloadArraySchemaResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Tag("unit")
@DisplayName("Unit Test: OpenApiGenericsSchemaAutoConfiguration")
class OpenApiGenericsSchemaAutoConfigurationTest {

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(OpenApiGenericsSchemaAutoConfiguration.class));

  @Test
  @DisplayName("should register schema infrastructure beans when Springdoc is present")
  void shouldRegisterSchemaInfrastructureBeans() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(ArrayItemReferenceExtractor.class);
          assertThat(context).hasSingleBean(ComponentContainerSchemaResolver.class);
          assertThat(context).hasSingleBean(WrapperPayloadArraySchemaResolver.class);
          assertThat(context).hasSingleBean(ContainerSchemaMetadataResolver.class);
          assertThat(context).hasSingleBean(WrapperSchemaEnricher.class);
          assertThat(context).hasSingleBean(WrapperSchemaProcessor.class);
        });
  }

  @Test
  @DisplayName("should not load schema auto-configuration when Springdoc is missing")
  void shouldNotLoadWhenSpringdocMissing() {
    contextRunner
        .withClassLoader(new FilteredClassLoader(OpenApiCustomizer.class))
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(ArrayItemReferenceExtractor.class);
              assertThat(context).doesNotHaveBean(ComponentContainerSchemaResolver.class);
              assertThat(context).doesNotHaveBean(WrapperPayloadArraySchemaResolver.class);
              assertThat(context).doesNotHaveBean(ContainerSchemaMetadataResolver.class);
              assertThat(context).doesNotHaveBean(WrapperSchemaEnricher.class);
              assertThat(context).doesNotHaveBean(WrapperSchemaProcessor.class);
            });
  }

  @Test
  @DisplayName("should back off when user provides custom ArrayItemReferenceExtractor")
  void shouldBackOffForCustomArrayItemReferenceExtractor() {
    contextRunner
        .withUserConfiguration(CustomArrayItemReferenceExtractorConfig.class)
        .run(
            context -> {
              ArrayItemReferenceExtractor extractor =
                  context.getBean(ArrayItemReferenceExtractor.class);

              assertThat(extractor)
                  .isSameAs(CustomArrayItemReferenceExtractorConfig.CUSTOM_EXTRACTOR);
            });
  }

  @Test
  @DisplayName("should back off when user provides custom ComponentContainerSchemaResolver")
  void shouldBackOffForCustomComponentContainerSchemaResolver() {
    contextRunner
        .withUserConfiguration(CustomComponentContainerSchemaResolverConfig.class)
        .run(
            context -> {
              ComponentContainerSchemaResolver resolver =
                  context.getBean(ComponentContainerSchemaResolver.class);

              assertThat(resolver)
                  .isSameAs(CustomComponentContainerSchemaResolverConfig.CUSTOM_RESOLVER);
            });
  }

  @Test
  @DisplayName("should back off when user provides custom WrapperPayloadArraySchemaResolver")
  void shouldBackOffForCustomWrapperPayloadArraySchemaResolver() {
    contextRunner
        .withUserConfiguration(CustomWrapperPayloadArraySchemaResolverConfig.class)
        .run(
            context -> {
              WrapperPayloadArraySchemaResolver resolver =
                  context.getBean(WrapperPayloadArraySchemaResolver.class);

              assertThat(resolver)
                  .isSameAs(CustomWrapperPayloadArraySchemaResolverConfig.CUSTOM_RESOLVER);
            });
  }

  @Test
  @DisplayName("should back off when user provides custom ContainerSchemaMetadataResolver")
  void shouldBackOffForCustomContainerSchemaMetadataResolver() {
    contextRunner
        .withUserConfiguration(CustomContainerSchemaMetadataResolverConfig.class)
        .run(
            context -> {
              ContainerSchemaMetadataResolver resolver =
                  context.getBean(ContainerSchemaMetadataResolver.class);

              assertThat(resolver)
                  .isSameAs(CustomContainerSchemaMetadataResolverConfig.CUSTOM_RESOLVER);
            });
  }

  @Test
  @DisplayName("should back off when user provides custom WrapperSchemaEnricher")
  void shouldBackOffForCustomWrapperSchemaEnricher() {
    contextRunner
        .withUserConfiguration(CustomWrapperSchemaEnricherConfig.class)
        .run(
            context -> {
              WrapperSchemaEnricher enricher = context.getBean(WrapperSchemaEnricher.class);

              assertThat(enricher).isSameAs(CustomWrapperSchemaEnricherConfig.CUSTOM_ENRICHER);
            });
  }

  @Test
  @DisplayName("should back off when user provides custom WrapperSchemaProcessor")
  void shouldBackOffForCustomWrapperSchemaProcessor() {
    contextRunner
        .withUserConfiguration(CustomWrapperSchemaProcessorConfig.class)
        .run(
            context -> {
              WrapperSchemaProcessor processor = context.getBean(WrapperSchemaProcessor.class);

              assertThat(processor).isSameAs(CustomWrapperSchemaProcessorConfig.CUSTOM_PROCESSOR);
            });
  }

  @Configuration
  static class CustomArrayItemReferenceExtractorConfig {

    static final ArrayItemReferenceExtractor CUSTOM_EXTRACTOR = new ArrayItemReferenceExtractor();

    @Bean
    ArrayItemReferenceExtractor arrayItemReferenceExtractor() {
      return CUSTOM_EXTRACTOR;
    }
  }

  @Configuration
  static class CustomComponentContainerSchemaResolverConfig {

    static final ComponentContainerSchemaResolver CUSTOM_RESOLVER =
        new ComponentContainerSchemaResolver();

    @Bean
    ComponentContainerSchemaResolver componentContainerSchemaResolver() {
      return CUSTOM_RESOLVER;
    }
  }

  @Configuration
  static class CustomWrapperPayloadArraySchemaResolverConfig {

    static final WrapperPayloadArraySchemaResolver CUSTOM_RESOLVER =
        new WrapperPayloadArraySchemaResolver();

    @Bean
    WrapperPayloadArraySchemaResolver wrapperPayloadArraySchemaResolver() {
      return CUSTOM_RESOLVER;
    }
  }

  @Configuration
  static class CustomContainerSchemaMetadataResolverConfig {

    static final ContainerSchemaMetadataResolver CUSTOM_RESOLVER =
        new ContainerSchemaMetadataResolver(
            new WrapperPayloadArraySchemaResolver(),
            new ComponentContainerSchemaResolver(),
            new ArrayItemReferenceExtractor());

    @Bean
    ContainerSchemaMetadataResolver containerSchemaMetadataResolver() {
      return CUSTOM_RESOLVER;
    }
  }

  @Configuration
  static class CustomWrapperSchemaEnricherConfig {

    static final WrapperSchemaEnricher CUSTOM_ENRICHER =
        new WrapperSchemaEnricher(
            new ContainerSchemaMetadataResolver(
                new WrapperPayloadArraySchemaResolver(),
                new ComponentContainerSchemaResolver(),
                new ArrayItemReferenceExtractor()));

    @Bean
    WrapperSchemaEnricher wrapperSchemaEnricher() {
      return CUSTOM_ENRICHER;
    }
  }

  @Configuration
  static class CustomWrapperSchemaProcessorConfig {

    static final WrapperSchemaProcessor CUSTOM_PROCESSOR =
        new WrapperSchemaProcessor(
            new WrapperSchemaEnricher(
                new ContainerSchemaMetadataResolver(
                    new WrapperPayloadArraySchemaResolver(),
                    new ComponentContainerSchemaResolver(),
                    new ArrayItemReferenceExtractor())));

    @Bean
    WrapperSchemaProcessor wrapperSchemaProcessor() {
      return CUSTOM_PROCESSOR;
    }
  }
}
