package io.github.blueprintplatform.openapi.generics.server.core.schema.extractor;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

/**
 * Extracts item type from List<T> style schemas.
 * List<T> is usually represented as a direct array in OpenAPI.
 */
public class ListItemExtractor implements ItemExtractor {

    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

    @Override
    public String extractItemName(Schema<?> containerSchema, Map<String, Schema> allSchemas) {
        if (containerSchema == null) return null;

        Schema<?> items = null;

        if (containerSchema instanceof ArraySchema arraySchema) {
            items = arraySchema.getItems();
        } else if ("array".equals(containerSchema.getType())) {
            items = containerSchema.getItems();
        } else if (containerSchema instanceof JsonSchema jsonSchema
                && jsonSchema.getTypes() != null
                && jsonSchema.getTypes().contains("array")) {
            items = jsonSchema.getItems();
        }

        if (items == null) return null;

        String itemRef = items.get$ref();
        if (itemRef == null || !itemRef.startsWith(SCHEMA_REF_PREFIX)) {
            return null;
        }

        return itemRef.substring(SCHEMA_REF_PREFIX.length());
    }
}