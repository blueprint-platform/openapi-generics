package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.SupportedContainerType;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;

public interface ContainerSchemaStrategy {

  SupportedContainerType containerType();

  ContainerSchemaResolver resolver();

  ItemExtractor extractor();

  default String containerName() {
    return containerType().containerName();
  }
}
