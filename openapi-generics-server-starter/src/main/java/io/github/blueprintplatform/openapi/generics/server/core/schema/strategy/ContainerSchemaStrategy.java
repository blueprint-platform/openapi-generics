package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;

public interface ContainerSchemaStrategy {

    String containerName();

    ContainerSchemaResolver resolver();

    ItemExtractor extractor();
}