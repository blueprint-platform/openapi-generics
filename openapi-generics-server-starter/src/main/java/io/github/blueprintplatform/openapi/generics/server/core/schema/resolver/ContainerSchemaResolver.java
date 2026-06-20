package io.github.blueprintplatform.openapi.generics.server.core.schema.resolver;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

public interface ContainerSchemaResolver {

    Schema<?> resolve(Map<String, Schema> schemas, String dataRefName, String wrapperName);
}