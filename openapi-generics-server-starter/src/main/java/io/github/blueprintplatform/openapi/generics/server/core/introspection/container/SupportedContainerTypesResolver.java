package io.github.blueprintplatform.openapi.generics.server.core.introspection.container;

import java.util.Set;

public interface SupportedContainerTypesResolver {

  Set<SupportedContainerType> resolve();
}
