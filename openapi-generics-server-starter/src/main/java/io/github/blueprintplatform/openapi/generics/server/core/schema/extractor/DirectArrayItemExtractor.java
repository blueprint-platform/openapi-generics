package io.github.blueprintplatform.openapi.generics.server.core.schema.extractor;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.COMPONENT_SCHEMA_REF_PREFIX;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.TYPE_ARRAY;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Extracts item type from List<T> style schemas.
 * List<T> is usually represented as a direct array in OpenAPI.
 */
public class DirectArrayItemExtractor implements ItemExtractor {


    @SuppressWarnings("rawtypes")
    @Override
    public String extractItemName(Schema<?> containerSchema) {
        if (containerSchema == null) return null;

        Schema<?> items = null;

        if (containerSchema instanceof ArraySchema arraySchema) {
            items = arraySchema.getItems();
        } else if (TYPE_ARRAY.equals(containerSchema.getType())) {
            items = containerSchema.getItems();
        } else if (containerSchema instanceof JsonSchema jsonSchema
                && jsonSchema.getTypes() != null
                && jsonSchema.getTypes().contains(TYPE_ARRAY)) {
            items = jsonSchema.getItems();
        }

        if (items == null) return null;

        String itemRef = items.get$ref();
        if (itemRef == null || !itemRef.startsWith(COMPONENT_SCHEMA_REF_PREFIX)) {
            return null;
        }

        return itemRef.substring(COMPONENT_SCHEMA_REF_PREFIX.length());
    }
}