package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaEnricher;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ContentArrayItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.DirectArrayItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ComponentContainerSchemaResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.WrapperPayloadArraySchemaResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.*;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = OpenApiGenericsAutoConfiguration.class)
@ConditionalOnClass(OpenApiCustomizer.class)
@ConditionalOnWebApplication
public class OpenApiGenericsSchemaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ContentArrayItemExtractor.class)
    public ContentArrayItemExtractor contentArrayItemExtractor() {
        return new ContentArrayItemExtractor();
    }

    @Bean
    @ConditionalOnMissingBean(DirectArrayItemExtractor.class)
    public DirectArrayItemExtractor directArrayItemExtractor() {
        return new DirectArrayItemExtractor();
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
    @ConditionalOnMissingBean(PageContainerSchemaStrategy.class)
    public PageContainerSchemaStrategy pageContainerSchemaStrategy(
            ComponentContainerSchemaResolver componentContainerSchemaResolver, ContentArrayItemExtractor contentArrayItemExtractor) {
        return new PageContainerSchemaStrategy(componentContainerSchemaResolver, contentArrayItemExtractor);
    }

    @Bean
    @ConditionalOnMissingBean(ListContainerSchemaStrategy.class)
    public ListContainerSchemaStrategy listContainerSchemaStrategy(
            WrapperPayloadArraySchemaResolver wrapperPayloadArraySchemaResolver,
            DirectArrayItemExtractor directArrayItemExtractor) {
        return new ListContainerSchemaStrategy(wrapperPayloadArraySchemaResolver, directArrayItemExtractor);
    }

    @Bean
    @ConditionalOnMissingBean(SetContainerSchemaStrategy.class)
    public SetContainerSchemaStrategy setContainerSchemaStrategy(
            WrapperPayloadArraySchemaResolver wrapperPayloadArraySchemaResolver,
            DirectArrayItemExtractor directArrayItemExtractor) {
        return new SetContainerSchemaStrategy(
                wrapperPayloadArraySchemaResolver,
                directArrayItemExtractor);
    }

    @Bean
    @ConditionalOnMissingBean
    public ContainerSchemaRegistry containerSchemaRegistry(List<ContainerSchemaStrategy> strategies) {
        return new ContainerSchemaRegistry(strategies);
    }

    @Bean
    @ConditionalOnMissingBean
    public WrapperSchemaEnricher wrapperSchemaEnricher(ContainerSchemaRegistry registry) {
        return new WrapperSchemaEnricher(registry);
    }
}