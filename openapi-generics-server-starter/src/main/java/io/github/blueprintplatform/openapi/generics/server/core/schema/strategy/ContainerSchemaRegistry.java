package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.SupportedContainerType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContainerSchemaRegistry {

  private final Map<SupportedContainerType, ContainerSchemaStrategy> strategies =
      new LinkedHashMap<>();

  public ContainerSchemaRegistry(List<ContainerSchemaStrategy> strategies) {
    if (strategies != null) {
      strategies.forEach(strategy -> this.strategies.put(strategy.containerType(), strategy));
    }
  }

  public ContainerSchemaStrategy findByContainerType(SupportedContainerType containerType) {
    if (containerType == null) {
      return null;
    }

    return strategies.get(containerType);
  }
}
