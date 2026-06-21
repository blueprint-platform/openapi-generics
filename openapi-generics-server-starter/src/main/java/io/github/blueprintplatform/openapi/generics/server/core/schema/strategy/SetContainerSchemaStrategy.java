package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.SET;

import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;

public record SetContainerSchemaStrategy(
        ContainerSchemaResolver resolver,
        ItemExtractor extractor)
        implements ContainerSchemaStrategy {

    @Override
    public String containerName() {
        return SET;
    }
}