package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;

public interface ContainerSchemaStrategy {

    String containerName();

    ContainerSchemaResolver resolver();

    ItemExtractor extractor();

    default boolean matches(String dataRefName) {
        String container = containerName();

        if (dataRefName == null || !dataRefName.startsWith(container)) {
            return false;
        }

        if (dataRefName.length() == container.length()) {
            return true;
        }

        return Character.isUpperCase(dataRefName.charAt(container.length()));
    }
}