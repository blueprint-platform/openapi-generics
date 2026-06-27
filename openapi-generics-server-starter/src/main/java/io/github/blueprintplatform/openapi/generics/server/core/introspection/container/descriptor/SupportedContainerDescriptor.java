package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor;

import java.util.Objects;

/**
 * Describes a supported generic container contract recognized by the projection pipeline.
 *
 * <p>A container descriptor separates Java identity, OpenAPI schema identity, semantic identity,
 * and schema-shape behavior. This allows built-in containers and configured BYOC containers to pass
 * through the same deterministic introspection and projection pipeline.
 *
 * @param type raw Java container type discovered during introspection
 * @param schemaName canonical schema identifier used during projection
 * @param containerName semantic container identifier exposed through vendor extensions
 * @param shape OpenAPI schema shape of the container
 * @param itemPropertyName JSON property containing the generic item collection for object
 *     containers
 * @param source descriptor source
 * @param matchMode Java type matching policy
 */
public record SupportedContainerDescriptor(
    Class<?> type,
    String schemaName,
    String containerName,
    ContainerShape shape,
    String itemPropertyName,
    ContainerSource source,
    ContainerMatchMode matchMode) {

  public SupportedContainerDescriptor {
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(shape, "shape must not be null");
    Objects.requireNonNull(source, "source must not be null");
    Objects.requireNonNull(matchMode, "matchMode must not be null");

    if (schemaName == null || schemaName.isBlank()) {
      throw new IllegalArgumentException("schemaName must not be null or blank");
    }

    if (containerName == null || containerName.isBlank()) {
      throw new IllegalArgumentException("containerName must not be null or blank");
    }

    if (shape == ContainerShape.OBJECT_WITH_ITEM_ARRAY
        && (itemPropertyName == null || itemPropertyName.isBlank())) {
      throw new IllegalArgumentException(
          "itemPropertyName must not be null or blank for object containers");
    }

    if (shape == ContainerShape.DIRECT_ARRAY && itemPropertyName != null) {
      throw new IllegalArgumentException(
          "itemPropertyName must be null for direct array containers");
    }
  }

  public String containerTypeName() {
    return type.getName();
  }

  public boolean matches(Class<?> candidate) {
    if (candidate == null) {
      return false;
    }

    return switch (matchMode) {
      case EXACT -> type.equals(candidate);
      case ASSIGNABLE -> type.isAssignableFrom(candidate);
    };
  }
}
