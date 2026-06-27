package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import java.util.Set;

public interface SupportedContainerTypesResolver {

  Set<SupportedContainerDescriptor> resolve();
}
