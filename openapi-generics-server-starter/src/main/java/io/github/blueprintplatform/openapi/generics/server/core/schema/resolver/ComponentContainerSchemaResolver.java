package io.github.blueprintplatform.openapi.generics.server.core.schema.resolver;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.*;

import io.swagger.v3.oas.models.media.*;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ComponentContainerSchemaResolver implements ContainerSchemaResolver {

    @SuppressWarnings("rawtypes")
    @Override
    public Schema<?> resolve(
            Map<String, Schema> schemas,
            String dataRefName,
            String wrapperName,
            String payloadPropertyName) {

        if (!schemas.containsKey(dataRefName)) {
            return null;
        }

        return resolveContainerSchema(schemas, schemas.get(dataRefName), new LinkedHashSet<>());
    }

    @SuppressWarnings("rawtypes")
    private Schema<?> resolveContainerSchema(
            Map<String, Schema> schemas, Schema<?> schema, Set<String> visited) {

        if (schema == null) return null;

        Schema<?> current = dereferenceIfNeeded(schemas, schema, visited);
        if (current == null) return null;

        if (isContainerLike(current)) return current;

        if (current instanceof ComposedSchema composed && composed.getAllOf() != null) {
            for (Schema<?> candidate : composed.getAllOf()) {
                Schema<?> resolved = resolveContainerSchema(schemas, candidate, visited);
                if (resolved != null) return resolved;
            }
        }

        return null;
    }

    @SuppressWarnings("rawtypes")
    private Schema<?> dereferenceIfNeeded(
            Map<String, Schema> schemas, Schema<?> schema, Set<String> visited) {

        String ref = schema.get$ref();
        if (ref == null || !ref.startsWith(COMPONENT_SCHEMA_REF_PREFIX)) return schema;

        String name = ref.substring(COMPONENT_SCHEMA_REF_PREFIX.length());
        if (!visited.add(name)) return null;

        return schemas.get(name);
    }

    private boolean isContainerLike(Schema<?> schema) {
        return schema instanceof ObjectSchema
                || TYPE_OBJECT.equals(schema.getType())
                || (schema.getProperties() != null && !schema.getProperties().isEmpty())
                || isArrayLike(schema);
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