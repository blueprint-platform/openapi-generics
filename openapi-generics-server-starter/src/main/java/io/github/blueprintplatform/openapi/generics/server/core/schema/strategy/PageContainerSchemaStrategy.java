package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;

public record PageContainerSchemaStrategy(ContainerSchemaResolver resolver, ItemExtractor extractor)
    implements ContainerSchemaStrategy {

    @Override
    public String containerName() {
        return Page.class.getSimpleName();
    }
}