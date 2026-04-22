package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@Tag("unit")
@DisplayName("Unit Test: OpenApiGenericsMissingDependencyAutoConfiguration")
class OpenApiGenericsMissingDependencyAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(OpenApiGenericsMissingDependencyAutoConfiguration.class));

    @Test
    @DisplayName("should register warning bean when Springdoc is missing")
    void shouldRegisterBeanWhenSpringdocMissing() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(OpenApiCustomizer.class))
                .run(
                        context -> {
                            assertThat(context)
                                    .hasSingleBean(OpenApiGenericsMissingDependencyAutoConfiguration.class);
                        });
    }

    @Test
    @DisplayName("should not register warning bean when Springdoc is present")
    void shouldNotRegisterBeanWhenSpringdocPresent() {
        contextRunner.run(
                context -> {
                    assertThat(context)
                            .doesNotHaveBean(OpenApiGenericsMissingDependencyAutoConfiguration.class);
                });
    }

    @Test
    @DisplayName("should invoke PostConstruct without exception when activated")
    void shouldInvokePostConstructWithoutException() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(OpenApiCustomizer.class))
                .run(
                        context -> {
                            OpenApiGenericsMissingDependencyAutoConfiguration bean =
                                    context.getBean(OpenApiGenericsMissingDependencyAutoConfiguration.class);
                            bean.logWarning(); // idempotent, should not throw
                        });
    }
}