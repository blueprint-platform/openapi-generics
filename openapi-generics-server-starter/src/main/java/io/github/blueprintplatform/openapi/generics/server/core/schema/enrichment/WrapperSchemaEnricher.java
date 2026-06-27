package io.github.blueprintplatform.openapi.generics.server.core.schema.enrichment;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

/**
 * Enriches projected wrapper schemas with container metadata for generic client reconstruction.
 *
 * <p>Adds vendor extensions describing the generic container contract and its payload item type.
 */
public class WrapperSchemaEnricher {
  private final ContainerSchemaMetadataResolver metadataResolver;

  public WrapperSchemaEnricher(ContainerSchemaMetadataResolver metadataResolver) {
    this.metadataResolver = metadataResolver;
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

    ContainerSchemaMetadata metadata = metadataResolver.resolve(schemas, wrapperName, descriptor);

    if (metadata == null) {
      return;
    }

    applyContainerMetadata(metadata);
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
}
