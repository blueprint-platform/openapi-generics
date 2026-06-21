package io.github.blueprintplatform.openapi.generics.server.core.introspection.container;

import java.util.Objects;

/**
 * Describes a supported generic container type recognized by the projection pipeline.
 *
 * <p>A container definition separates two distinct concerns:
 *
 * <ul>
 *   <li><b>Schema name</b> used for OpenAPI schema identification and projection
 *   <li><b>Container name</b> used as the semantic identifier exposed through vendor extensions
 * </ul>
 *
 * <p>For built-in containers these values are often identical:
 *
 * <pre>
 * Page  -> schemaName=Page, containerName=Page
 * List  -> schemaName=List, containerName=List
 * </pre>
 *
 * <p>They are modeled separately to allow future container implementations whose schema
 * representation and semantic identity may differ.
 *
 * <p>This type contains only container metadata and matching logic. Container-specific
 * schema resolution, item extraction, and code generation behavior are handled by the
 * corresponding container infrastructure.
 *
 * @param type raw container type
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

    public boolean matches(Class<?> candidate) {
        return candidate != null && type.isAssignableFrom(candidate);
    }
}