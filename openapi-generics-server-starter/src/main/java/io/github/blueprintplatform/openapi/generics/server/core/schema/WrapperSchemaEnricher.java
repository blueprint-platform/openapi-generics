package io.github.blueprintplatform.openapi.generics.server.core.schema;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ContainerSchemaRegistry;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ContainerSchemaStrategy;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

/**
 * Enriches projected wrapper schemas with container metadata required for generic reconstruction.
 *
 * <p>This component resolves the container represented by a wrapper payload schema and applies the
 * corresponding OpenAPI Generics vendor extensions to the wrapper schema.
 *
 * <p>The actual container-specific behavior is delegated to {@link ContainerSchemaStrategy}
 * implementations registered in {@link ContainerSchemaRegistry}.
 */
public class WrapperSchemaEnricher {

  private final ContainerSchemaRegistry containerSchemaRegistry;

  public WrapperSchemaEnricher(ContainerSchemaRegistry containerSchemaRegistry) {
    this.containerSchemaRegistry = containerSchemaRegistry;
  }

  @SuppressWarnings("rawtypes")
  public void enrich(OpenAPI openApi, String wrapperName, ResponseTypeDescriptor descriptor) {
    Map<String, Schema> schemas = getSchemas(openApi);

    if (schemas.isEmpty()
        || wrapperName == null
        || descriptor == null
        || !descriptor.isContainer()) {
      return;
    }

    ContainerSchemaMetadata metadata = resolveContainerMetadata(schemas, wrapperName, descriptor);

    if (metadata == null) {
      return;
    }

    applyContainerMetadata(metadata);
  }

  @SuppressWarnings("rawtypes")
  private ContainerSchemaMetadata resolveContainerMetadata(
      Map<String, Schema> schemas, String wrapperName, ResponseTypeDescriptor descriptor) {
    ContainerSchemaStrategy strategy =
        containerSchemaRegistry.findByContainerType(descriptor.containerType());

    if (strategy == null) {
      return null;
    }

    Schema<?> containerSchema =
        strategy
            .resolver()
            .resolve(
                schemas, descriptor.dataRefName(), wrapperName, descriptor.payloadPropertyName());

    if (containerSchema == null) {
      return null;
    }

    String itemName = strategy.extractor().extractItemName(containerSchema);

    if (itemName == null) {
      return null;
    }

    Schema<?> wrapper = schemas.get(wrapperName);

    if (wrapper == null) {
      return null;
    }

    return new ContainerSchemaMetadata(
        wrapper, strategy.containerName(), descriptor.containerTypeName(), itemName);
  }

  private void applyContainerMetadata(ContainerSchemaMetadata metadata) {
    metadata.wrapper().addExtension(VendorExtensions.DATA_CONTAINER, metadata.containerName());
    metadata
        .wrapper()
        .addExtension(VendorExtensions.DATA_CONTAINER_TYPE, metadata.containerTypeName());
    metadata.wrapper().addExtension(VendorExtensions.DATA_ITEM, metadata.itemName());
  }

  @SuppressWarnings("rawtypes")
  private Map<String, Schema> getSchemas(OpenAPI openApi) {
    if (openApi == null
        || openApi.getComponents() == null
        || openApi.getComponents().getSchemas() == null) {
      return Map.of();
    }

    return openApi.getComponents().getSchemas();
  }

  private record ContainerSchemaMetadata(
      Schema<?> wrapper, String containerName, String containerTypeName, String itemName) {}
}
