package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;
import java.util.List;

public class ListContainerSchemaStrategy implements ContainerSchemaStrategy {

    private final ContainerSchemaResolver resolver;
    private final ItemExtractor extractor;

    public ListContainerSchemaStrategy(ContainerSchemaResolver resolver, ItemExtractor extractor) {
        this.resolver = resolver;
        this.extractor = extractor;
    }

    @Override
    public String containerName() {
        return List.class.getSimpleName();
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