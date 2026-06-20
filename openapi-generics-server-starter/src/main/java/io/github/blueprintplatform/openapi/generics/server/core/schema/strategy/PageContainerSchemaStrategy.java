package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;

public class PageContainerSchemaStrategy implements ContainerSchemaStrategy {

    private final ContainerSchemaResolver resolver;
    private final ItemExtractor extractor;

    public PageContainerSchemaStrategy(ContainerSchemaResolver resolver, ItemExtractor extractor) {
        this.resolver = resolver;
        this.extractor = extractor;
    }

    @Override
    public String containerName() {
        return Page.class.getSimpleName();
    }

    @Override
    public ContainerSchemaResolver resolver() {
        return resolver;
    }

    @Override
    public ItemExtractor extractor() {
        return extractor;
    }
}