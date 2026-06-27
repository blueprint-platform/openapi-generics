package io.github.blueprintplatform.openapi.generics.server.core.schema.resolution;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.TYPE_ARRAY;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

/** Resolves array payload schemas defined directly on projected wrapper properties. */
public class WrapperPayloadArraySchemaResolver implements ContainerSchemaResolver {

  @SuppressWarnings("rawtypes")
  @Override
  public Schema<?> resolve(
      Map<String, Schema> schemas,
      String dataRefName,
      String wrapperName,
      String payloadPropertyName) {
    Schema<?> wrapper = schemas.get(wrapperName);
    if (wrapper == null) return null;

    @SuppressWarnings("rawtypes")
    Map<String, Schema> properties = wrapper.getProperties();
    if (properties == null) return null;

    Schema<?> dataProperty = properties.get(payloadPropertyName);
    if (dataProperty == null) return null;

    if (isArrayLike(dataProperty)) {
      return dataProperty;
    }
    return null;
  }

  private boolean isArrayLike(Schema<?> schema) {
    if (schema == null) return false;
    return schema instanceof ArraySchema
        || TYPE_ARRAY.equals(schema.getType())
        || (schema instanceof JsonSchema json
            && json.getTypes() != null
            && json.getTypes().contains(TYPE_ARRAY));
  }
}
