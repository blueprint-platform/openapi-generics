package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import java.util.ArrayList;
import java.util.List;

public class ContainerSchemaRegistry {

    private final List<ContainerSchemaStrategy> strategies;

    public ContainerSchemaRegistry(List<ContainerSchemaStrategy> strategies) {
        this.strategies = List.copyOf(new ArrayList<>(strategies));
    }

    public ContainerSchemaStrategy findByDataRefName(String dataRefName) {
        for (ContainerSchemaStrategy strategy : strategies) {
            if (strategy.matches(dataRefName)) {
                return strategy;
            }
        }

        return null;
    }
}