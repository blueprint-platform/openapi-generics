package io.github.blueprintplatform.openapi.generics.server.core.schema.resolution;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

public interface ContainerSchemaResolver {
  // Callers consume heterogeneous Swagger schema types through their shared API.
  @SuppressWarnings({"rawtypes", "java:S1452"})
  Schema<?> resolve(
      Map<String, Schema> schemas,
      String dataRefName,
      String wrapperName,
      String payloadPropertyName);
}
