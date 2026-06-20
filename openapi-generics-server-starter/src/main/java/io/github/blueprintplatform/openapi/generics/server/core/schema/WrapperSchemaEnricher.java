package io.github.blueprintplatform.openapi.generics.server.core.schema;

import io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ContainerSchemaRegistry;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ContainerSchemaStrategy;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

public class WrapperSchemaEnricher {

  private final ContainerSchemaRegistry containerSchemaRegistry;

  public WrapperSchemaEnricher(ContainerSchemaRegistry containerSchemaRegistry) {
    this.containerSchemaRegistry = containerSchemaRegistry;
  }

  public void enrich(OpenAPI openApi, String wrapperName, String dataRefName) {
    if (openApi == null || dataRefName == null || wrapperName == null) {
      return;
    }

    Map<String, Schema> schemas = getSchemas(openApi);

    if (schemas.isEmpty()) {
      return;
    }

    ContainerSchemaStrategy strategy = containerSchemaRegistry.findByDataRefName(dataRefName);

    if (strategy == null) {
      return;
    }

    Schema<?> containerSchema = strategy.resolver().resolve(schemas, dataRefName, wrapperName);

    if (containerSchema == null) {
      return;
    }

    String itemName = strategy.extractor().extractItemName(containerSchema, schemas);

    if (itemName == null) {
      return;
    }

    Schema<?> wrapper = schemas.get(wrapperName);

    if (wrapper == null) {
      return;
    }

    wrapper.addExtension(VendorExtensions.DATA_CONTAINER, strategy.containerName());
    wrapper.addExtension(VendorExtensions.DATA_ITEM, itemName);
  }

  private Map<String, Schema> getSchemas(OpenAPI openApi) {
    if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
      return Map.of();
    }

    return openApi.getComponents().getSchemas();
  }
}