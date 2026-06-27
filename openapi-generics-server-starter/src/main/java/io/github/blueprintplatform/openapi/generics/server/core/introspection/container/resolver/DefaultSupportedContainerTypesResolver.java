package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.*;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.PROPERTY_CONTENT;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Provides descriptor definitions for the built-in generic container contracts. */
public final class DefaultSupportedContainerTypesResolver
    implements SupportedContainerTypesResolver {

  @Override
  public Set<SupportedContainerDescriptor> resolve() {
    Set<SupportedContainerDescriptor> containers = new LinkedHashSet<>();

    containers.add(
        new SupportedContainerDescriptor(
            Page.class,
            PAGE,
            PAGE,
            ContainerShape.OBJECT_WITH_ITEM_ARRAY,
            PROPERTY_CONTENT,
            ContainerSource.BUILT_IN,
            ContainerMatchMode.EXACT));

    containers.add(
        new SupportedContainerDescriptor(
            List.class,
            LIST,
            LIST,
            ContainerShape.DIRECT_ARRAY,
            null,
            ContainerSource.BUILT_IN,
            ContainerMatchMode.ASSIGNABLE));

    containers.add(
        new SupportedContainerDescriptor(
            Set.class,
            SET,
            SET,
            ContainerShape.DIRECT_ARRAY,
            null,
            ContainerSource.BUILT_IN,
            ContainerMatchMode.ASSIGNABLE));

    return Set.copyOf(containers);
  }
}
