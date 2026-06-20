package io.github.blueprintplatform.openapi.generics.server.core.schema.resolver;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DirectSchemaResolver implements ContainerSchemaResolver {

    @Override
    public Schema<?> resolve(Map<String, Schema> schemas, String dataRefName, String wrapperName) {
        if (!schemas.containsKey(dataRefName)) {
            return null;
        }
        return resolveContainerSchema(schemas, schemas.get(dataRefName), new LinkedHashSet<>());
    }

    private Schema<?> resolveContainerSchema(Map<String, Schema> schemas, Schema<?> schema, Set<String> visited) {
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

    private Schema<?> dereferenceIfNeeded(Map<String, Schema> schemas, Schema<?> schema, Set<String> visited) {
        String ref = schema.get$ref();
        if (ref == null || !ref.startsWith("#/components/schemas/")) return schema;

        String name = ref.substring("#/components/schemas/".length());
        if (!visited.add(name)) return null;

        return schemas.get(name);
    }

    private boolean isContainerLike(Schema<?> schema) {
        return schema instanceof io.swagger.v3.oas.models.media.ObjectSchema
                || "object".equals(schema.getType())
                || (schema.getProperties() != null && !schema.getProperties().isEmpty())
                || isArrayLike(schema);
    }

    private boolean isArrayLike(Schema<?> schema) {
        if (schema == null) return false;
        return schema instanceof io.swagger.v3.oas.models.media.ArraySchema
                || "array".equals(schema.getType())
                || (schema instanceof io.swagger.v3.oas.models.media.JsonSchema json
                && json.getTypes() != null && json.getTypes().contains("array"));
    }
}