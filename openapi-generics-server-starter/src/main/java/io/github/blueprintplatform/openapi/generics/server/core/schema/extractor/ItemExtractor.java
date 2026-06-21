package io.github.blueprintplatform.openapi.generics.server.core.schema.extractor;

import io.swagger.v3.oas.models.media.Schema;

/**
 * Strategy interface for extracting the item type name from a container schema.
 * Used by WrapperSchemaEnricher to support different container types
 * (List, Page, Slice, etc.) in a clean and extensible way.
 */
public interface ItemExtractor {

    /**
     * Extracts the simple name of the item type inside the container.
     *
     * @param containerSchema the schema representing the container
     * @return the simple name of the item type, or null if it cannot be determined
     */
    String extractItemName(Schema<?> containerSchema);
}