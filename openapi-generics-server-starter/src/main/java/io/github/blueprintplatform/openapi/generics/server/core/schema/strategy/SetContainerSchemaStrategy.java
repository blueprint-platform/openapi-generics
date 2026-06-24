package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.SET;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.SupportedContainerType;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;
import java.util.Set;

public record SetContainerSchemaStrategy(ContainerSchemaResolver resolver, ItemExtractor extractor)
    implements ContainerSchemaStrategy {

  private static final SupportedContainerType CONTAINER_TYPE =
      new SupportedContainerType(Set.class, SET, SET);

  @Override
  public SupportedContainerType containerType() {
    return CONTAINER_TYPE;
  }
}
