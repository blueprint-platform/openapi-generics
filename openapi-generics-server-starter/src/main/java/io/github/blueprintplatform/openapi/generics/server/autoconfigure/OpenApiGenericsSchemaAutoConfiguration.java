package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaEnricher;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ListItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.PageItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.DirectSchemaResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.WrapperEmbeddedArrayResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ContainerSchemaRegistry;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ContainerSchemaStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ListContainerSchemaStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.PageContainerSchemaStrategy;
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
    @ConditionalOnMissingBean(PageItemExtractor.class)
    public PageItemExtractor pageItemExtractor() {
        return new PageItemExtractor();
    }

    @Bean
    @ConditionalOnMissingBean(ListItemExtractor.class)
    public ListItemExtractor listItemExtractor() {
        return new ListItemExtractor();
    }

    @Bean
    @ConditionalOnMissingBean(DirectSchemaResolver.class)
    public DirectSchemaResolver directSchemaResolver() {
        return new DirectSchemaResolver();
    }

    @Bean
    @ConditionalOnMissingBean(WrapperEmbeddedArrayResolver.class)
    public WrapperEmbeddedArrayResolver wrapperEmbeddedArrayResolver() {
        return new WrapperEmbeddedArrayResolver();
    }

    @Bean
    @ConditionalOnMissingBean(PageContainerSchemaStrategy.class)
    public PageContainerSchemaStrategy pageContainerSchemaStrategy(
            DirectSchemaResolver directSchemaResolver, PageItemExtractor pageItemExtractor) {
        return new PageContainerSchemaStrategy(directSchemaResolver, pageItemExtractor);
    }

    @Bean
    @ConditionalOnMissingBean(ListContainerSchemaStrategy.class)
    public ListContainerSchemaStrategy listContainerSchemaStrategy(
            WrapperEmbeddedArrayResolver wrapperEmbeddedArrayResolver,
            ListItemExtractor listItemExtractor) {
        return new ListContainerSchemaStrategy(wrapperEmbeddedArrayResolver, listItemExtractor);
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