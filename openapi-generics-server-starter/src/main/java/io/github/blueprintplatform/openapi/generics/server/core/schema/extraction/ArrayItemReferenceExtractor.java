package io.github.blueprintplatform.openapi.generics.server.core.schema.extraction;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.COMPONENT_SCHEMA_REF_PREFIX;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.TYPE_ARRAY;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;

/** Extracts the referenced component schema name from OpenAPI array item definitions. */
public class ArrayItemReferenceExtractor {

  public String extractItemName(Schema<?> arraySchema) {
    Schema<?> items = resolveItems(arraySchema);

    if (items == null) {
      return null;
    }

    String itemRef = items.get$ref();
    if (itemRef == null || !itemRef.startsWith(COMPONENT_SCHEMA_REF_PREFIX)) {
      return null;
    }

    return itemRef.substring(COMPONENT_SCHEMA_REF_PREFIX.length());
  }

  private Schema<?> resolveItems(Schema<?> schema) {
    if (schema == null) {
      return null;
    }

    if (schema instanceof ArraySchema arraySchema) {
      return arraySchema.getItems();
    }

    if (TYPE_ARRAY.equals(schema.getType())) {
      return schema.getItems();
    }

    if (schema instanceof JsonSchema jsonSchema
        && jsonSchema.getTypes() != null
        && jsonSchema.getTypes().contains(TYPE_ARRAY)) {
      return jsonSchema.getItems();
    }

    return null;
  }
}
