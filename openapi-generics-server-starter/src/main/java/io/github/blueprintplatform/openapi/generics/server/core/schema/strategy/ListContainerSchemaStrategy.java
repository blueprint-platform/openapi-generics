package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.LIST;

import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;

public record ListContainerSchemaStrategy(ContainerSchemaResolver resolver, ItemExtractor extractor)
        implements ContainerSchemaStrategy {

    @Override
    public String containerName() {
        return LIST;
    }
}