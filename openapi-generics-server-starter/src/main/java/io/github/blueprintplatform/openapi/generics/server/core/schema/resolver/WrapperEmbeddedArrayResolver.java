package io.github.blueprintplatform.openapi.generics.server.core.schema.resolver;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.PropertyNames.DATA;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.TYPE_ARRAY;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

public class WrapperEmbeddedArrayResolver implements ContainerSchemaResolver {

    @Override
    public Schema<?> resolve(Map<String, Schema> schemas, String dataRefName, String wrapperName) {
        Schema<?> wrapper = schemas.get(wrapperName);
        if (wrapper == null) return null;

        Map<String, Schema> properties = wrapper.getProperties();
        if (properties == null) return null;

        Schema<?> dataProperty = properties.get(DATA);
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
                && json.getTypes() != null && json.getTypes().contains(TYPE_ARRAY));
    }
}