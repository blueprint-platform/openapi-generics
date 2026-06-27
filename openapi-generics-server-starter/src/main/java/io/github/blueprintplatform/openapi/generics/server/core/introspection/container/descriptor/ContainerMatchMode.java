package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor;

/** Defines how discovered Java response types are matched against supported containers. */
public enum ContainerMatchMode {

  /** Candidate type must be exactly the configured container type. */
  EXACT,

  /** Candidate type may be assignable to the configured container type. */
  ASSIGNABLE
}
