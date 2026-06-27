package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor;

/** Describes how a generic container is represented in the OpenAPI schema. */
public enum ContainerShape {

  /** Container is represented directly as an array, for example List<T> or Set<T>. */
  DIRECT_ARRAY,

  /** Container is represented as an object containing an item collection property. */
  OBJECT_WITH_ITEM_ARRAY
}
