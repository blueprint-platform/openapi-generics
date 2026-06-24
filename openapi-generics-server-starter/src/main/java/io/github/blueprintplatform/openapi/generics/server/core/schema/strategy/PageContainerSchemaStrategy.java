package io.github.blueprintplatform.openapi.generics.server.core.schema.strategy;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.PAGE;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.SupportedContainerType;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ContainerSchemaResolver;

public record PageContainerSchemaStrategy(ContainerSchemaResolver resolver, ItemExtractor extractor)
    implements ContainerSchemaStrategy {

  private static final SupportedContainerType CONTAINER_TYPE =
      new SupportedContainerType(Page.class, PAGE, PAGE);

  @Override
  public SupportedContainerType containerType() {
    return CONTAINER_TYPE;
  }
}
