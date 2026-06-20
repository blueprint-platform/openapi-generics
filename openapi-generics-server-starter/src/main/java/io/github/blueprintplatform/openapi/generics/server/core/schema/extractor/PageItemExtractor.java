package io.github.blueprintplatform.openapi.generics.server.core.schema.extractor;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

/**
 * Extracts item type from Page<T> style schemas.
 * Page schema usually contains a "content" property which is an array.
 */
public class PageItemExtractor implements ItemExtractor {

    private static final String CONTENT = "content";
    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

    @Override
    public String extractItemName(Schema<?> containerSchema, Map<String, Schema> allSchemas) {
        if (containerSchema == null) return null;

        Map<String, Schema> properties = containerSchema.getProperties();
        if (properties == null) return null;

        Schema<?> content = properties.get(CONTENT);
        if (content == null) return null;

        Schema<?> items = null;

        if (content instanceof ArraySchema arraySchema) {
            items = arraySchema.getItems();
        } else if ("array".equals(content.getType())) {
            items = content.getItems();
        } else if (content instanceof JsonSchema jsonSchema
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