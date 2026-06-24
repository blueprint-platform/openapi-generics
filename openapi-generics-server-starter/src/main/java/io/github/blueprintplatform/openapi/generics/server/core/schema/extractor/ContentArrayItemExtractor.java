package io.github.blueprintplatform.openapi.generics.server.core.schema.extractor;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.*;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

/**
 * Extracts item type from Page<T> style schemas. Page schema usually contains a "content" property
 * which is an array.
 */
public class ContentArrayItemExtractor implements ItemExtractor {

  @SuppressWarnings("rawtypes")
  @Override
  public String extractItemName(Schema<?> containerSchema) {
    if (containerSchema == null) return null;

    Map<String, Schema> properties = containerSchema.getProperties();
    if (properties == null) return null;

    Schema<?> content = properties.get(PROPERTY_CONTENT);
    if (content == null) return null;

    Schema<?> items = null;

    if (content instanceof ArraySchema arraySchema) {
      items = arraySchema.getItems();
    } else if (TYPE_ARRAY.equals(content.getType())) {
      items = content.getItems();
    } else if (content instanceof JsonSchema jsonSchema
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
