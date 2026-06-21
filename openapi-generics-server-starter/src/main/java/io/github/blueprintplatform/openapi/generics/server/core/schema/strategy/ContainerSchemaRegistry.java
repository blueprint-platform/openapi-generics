package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContainerSchemaRegistry {

  private final Map<String, ContainerSchemaStrategy> strategies = new LinkedHashMap<>();

    public ContainerSchemaRegistry(List<ContainerSchemaStrategy> strategies) {
    if (strategies != null) {
      strategies.forEach(strategy -> this.strategies.put(strategy.containerName(), strategy));
    }
  }

  public ContainerSchemaStrategy findByContainerName(String containerName) {
    if (containerName == null || containerName.isBlank()) {
      return null;
    }

    return strategies.get(containerName);
    }
}