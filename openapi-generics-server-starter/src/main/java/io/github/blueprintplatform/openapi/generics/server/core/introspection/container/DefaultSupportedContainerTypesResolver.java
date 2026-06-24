package io.github.blueprintplatform.openapi.generics.server.core.introspection.container;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.*;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class DefaultSupportedContainerTypesResolver
    implements SupportedContainerTypesResolver {

  @Override
  public Set<SupportedContainerType> resolve() {
    Set<SupportedContainerType> containers = new LinkedHashSet<>();

    containers.add(new SupportedContainerType(Page.class, PAGE, PAGE));
    containers.add(new SupportedContainerType(List.class, LIST, LIST));
    containers.add(new SupportedContainerType(Set.class, SET, SET));

    return Set.copyOf(containers);
  }
}
