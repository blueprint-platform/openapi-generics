package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.LIST;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.SupportedContainerType;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;
import java.util.List;

public record ListContainerSchemaStrategy(ContainerSchemaResolver resolver, ItemExtractor extractor)
    implements ContainerSchemaStrategy {

  private static final SupportedContainerType CONTAINER_TYPE =
      new SupportedContainerType(List.class, LIST, LIST);

  @Override
  public SupportedContainerType containerType() {
    return CONTAINER_TYPE;
  }
}
