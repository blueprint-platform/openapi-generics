package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;
import java.util.List;

public record ListContainerSchemaStrategy(ContainerSchemaResolver resolver, ItemExtractor extractor)
    implements ContainerSchemaStrategy {

    @Override
    public String containerName() {
        return List.class.getSimpleName();
    }
}