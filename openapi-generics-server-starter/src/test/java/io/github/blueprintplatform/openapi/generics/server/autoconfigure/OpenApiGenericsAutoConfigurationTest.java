package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

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
@DisplayName("Unit Test: OpenApiGenericsAutoConfiguration")
class OpenApiGenericsAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(OpenApiGenericsAutoConfiguration.class));

    @Test
    @DisplayName("should register all default beans when Springdoc is present")
    void shouldRegisterAllDefaultBeans() {
        contextRunner.run(
                context -> {
                    assertThat(context).hasSingleBean(ResponseTypeDiscoveryStrategy.class);
                    assertThat(context).hasSingleBean(ResponseIntrospectionPolicyResolver.class);
                    assertThat(context).hasSingleBean(ResponseIntrospectionPolicy.class);
                    assertThat(context).hasSingleBean(ResponseTypeIntrospector.class);
                    assertThat(context).hasSingleBean(SchemaGenerationControlMarker.class);
                    assertThat(context).hasSingleBean(WrapperSchemaProcessor.class);
                    assertThat(context).hasSingleBean(WrapperSchemaEnricher.class);
                    assertThat(context).hasSingleBean(OpenApiContractGuard.class);
                    assertThat(context).hasSingleBean(OpenApiPipelineOrchestrator.class);
                    assertThat(context).hasBean("openApiGenericsCustomizer");
                });
    }

    @Test
    @DisplayName("should register MvcResponseTypeDiscoveryStrategy by default")
    void shouldRegisterMvcStrategyByDefault() {
        contextRunner.run(
                context -> {
                    ResponseTypeDiscoveryStrategy strategy =
                            context.getBean(ResponseTypeDiscoveryStrategy.class);
                    assertThat(strategy).isInstanceOf(MvcResponseTypeDiscoveryStrategy.class);
                });
    }

    @Test
    @DisplayName("should not load auto-configuration when Springdoc is missing")
    void shouldNotLoadWhenSpringdocMissing() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(OpenApiCustomizer.class))
                .run(
                        context -> {
                            assertThat(context).doesNotHaveBean(OpenApiPipelineOrchestrator.class);
                            assertThat(context).doesNotHaveBean("openApiGenericsCustomizer");
                        });
    }

    @Test
    @DisplayName("should back off when user provides custom ResponseTypeDiscoveryStrategy")
    void shouldBackOffForCustomDiscoveryStrategy() {
        contextRunner
                .withUserConfiguration(CustomDiscoveryStrategyConfig.class)
                .run(
                        context -> {
                            ResponseTypeDiscoveryStrategy strategy =
                                    context.getBean(ResponseTypeDiscoveryStrategy.class);
                            assertThat(strategy).isNotInstanceOf(MvcResponseTypeDiscoveryStrategy.class);
                        });
    }

    @Test
    @DisplayName("should back off when user provides custom OpenApiCustomizer with same name")
    void shouldBackOffForCustomOpenApiCustomizer() {
        contextRunner
                .withUserConfiguration(CustomOpenApiCustomizerConfig.class)
                .run(
                        context -> {
                            OpenApiCustomizer customizer =
                                    context.getBean("openApiGenericsCustomizer", OpenApiCustomizer.class);
                            assertThat(customizer).isNotNull();
                        });
    }

    @Test
    @DisplayName("should back off when user provides custom WrapperSchemaEnricher")
    void shouldBackOffForCustomEnricher() {
        contextRunner
                .withUserConfiguration(CustomEnricherConfig.class)
                .run(
                        context -> {
                            WrapperSchemaEnricher enricher = context.getBean(WrapperSchemaEnricher.class);
                            assertThat(enricher).isSameAs(CustomEnricherConfig.CUSTOM_ENRICHER);
                        });
    }

    // --- Test configurations ---

    @Configuration
    static class CustomDiscoveryStrategyConfig {
        @Bean
        ResponseTypeDiscoveryStrategy customStrategy() {
            return java.util.Collections::emptySet;
        }
    }

    @Configuration
    static class CustomOpenApiCustomizerConfig {
        @Bean
        OpenApiCustomizer openApiGenericsCustomizer() {
            return openApi -> {};
        }
    }

    @Configuration
    static class CustomEnricherConfig {
        static final WrapperSchemaEnricher CUSTOM_ENRICHER = new WrapperSchemaEnricher();

        @Bean
        WrapperSchemaEnricher wrapperSchemaEnricher() {
            return CUSTOM_ENRICHER;
        }
    }
}