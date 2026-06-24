package io.github.blueprintplatform.openapi.generics.server.core.introspection.container;

import java.util.Objects;

/**
 * Describes a supported generic container type recognized by the projection pipeline.
 *
 * <p>A container definition separates three distinct concerns:
 *
 * <ul>
 *   <li><b>Java type</b> discovered during response introspection
 *   <li><b>Schema name</b> used for OpenAPI schema identification and projection
 *   <li><b>Container name</b> used as the semantic identifier exposed through vendor extensions
 * </ul>
 *
 * <p>For built-in containers, schema and semantic names are often identical:
 *
 * <pre>
 * Page  -> schemaName=Page, containerName=Page
 * List  -> schemaName=List, containerName=List
 * Set   -> schemaName=Set,  containerName=Set
 * </pre>
 *
 * <p>The Java container type is preserved separately so projection metadata can expose the fully
 * qualified container identity without relying on schema names or naming conventions.
 *
 * @param type raw Java container type discovered during introspection
 * @param schemaName canonical schema identifier used during projection
 * @param containerName semantic container identifier exposed through vendor extensions
 */
public record SupportedContainerType(Class<?> type, String schemaName, String containerName) {

  public SupportedContainerType {
    Objects.requireNonNull(type, "type must not be null");

    if (schemaName == null || schemaName.isBlank()) {
      throw new IllegalArgumentException("schemaName must not be null or blank");
    }

    if (containerName == null || containerName.isBlank()) {
      throw new IllegalArgumentException("containerName must not be null or blank");
    }
  }

  public String containerTypeName() {
    return type.getName();
  }

  public boolean matches(Class<?> candidate) {
    return candidate != null && type.isAssignableFrom(candidate);
  }
}
