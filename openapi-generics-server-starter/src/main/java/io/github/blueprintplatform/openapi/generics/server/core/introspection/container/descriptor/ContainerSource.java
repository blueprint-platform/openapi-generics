package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor;

/** Identifies where a supported container definition comes from. */
public enum ContainerSource {

  /** Container provided by OpenAPI Generics itself. */
  BUILT_IN,

  /** Container explicitly configured by the application. */
  CONFIGURED
}
