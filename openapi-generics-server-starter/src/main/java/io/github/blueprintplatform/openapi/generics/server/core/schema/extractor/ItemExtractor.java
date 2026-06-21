package io.github.blueprintplatform.openapi.generics.server.core.schema.extractor;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

/**
 * Strategy interface for extracting the item type name from a container schema.
 * Used by WrapperSchemaEnricher to support different container types
 * (List, Page, Slice, etc.) in a clean and extensible way.
 */
public interface ItemExtractor {

    /**
     * Extracts the simple name of the item type inside the container.
     *
     * @param containerSchema the schema representing the container (e.g. array schema for List)
     * @param allSchemas      all schemas in the OpenAPI document (for $ref resolution)
     * @return the simple name of the item type, or null if cannot be determined
     */
    String extractItemName(Schema<?> containerSchema, Map<String, Schema> allSchemas);
}