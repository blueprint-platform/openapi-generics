package io.github.blueprintplatform.openapi.generics.server.core.schema.enrichment;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extraction.ArrayItemReferenceExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolution.ComponentContainerSchemaResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolution.WrapperPayloadArraySchemaResolver;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

/**
 * Resolves the container schema metadata required to enrich projected wrapper schemas.
 *
 * <p>Extracts the container type, item collection, and payload metadata used for generic client
 * reconstruction.
 */
public class ContainerSchemaMetadataResolver {

  private final WrapperPayloadArraySchemaResolver wrapperPayloadArraySchemaResolver;
  private final ComponentContainerSchemaResolver componentContainerSchemaResolver;
  private final ArrayItemReferenceExtractor arrayItemReferenceExtractor;

  public ContainerSchemaMetadataResolver(
      WrapperPayloadArraySchemaResolver wrapperPayloadArraySchemaResolver,
      ComponentContainerSchemaResolver componentContainerSchemaResolver,
      ArrayItemReferenceExtractor arrayItemReferenceExtractor) {
    this.wrapperPayloadArraySchemaResolver = wrapperPayloadArraySchemaResolver;
    this.componentContainerSchemaResolver = componentContainerSchemaResolver;
    this.arrayItemReferenceExtractor = arrayItemReferenceExtractor;
  }

  @SuppressWarnings("rawtypes")
  public ContainerSchemaMetadata resolve(
      Map<String, Schema> schemas, String wrapperName, ResponseTypeDescriptor descriptor) {
    if (schemas == null || schemas.isEmpty() || wrapperName == null || descriptor == null) {
      return null;
    }

    SupportedContainerDescriptor container = descriptor.container();
    if (container == null) {
      return null;
    }

    Schema<?> wrapper = schemas.get(wrapperName);
    if (wrapper == null) {
      return null;
    }

    Schema<?> containerSchema = resolveContainerSchema(schemas, wrapperName, descriptor, container);
    if (containerSchema == null) {
      return null;
    }

    Schema<?> itemArraySchema = resolveItemArraySchema(containerSchema, container);
    String itemName = arrayItemReferenceExtractor.extractItemName(itemArraySchema);

    if (itemName == null) {
      return null;
    }

    return new ContainerSchemaMetadata(
        wrapper, container.containerName(), container.containerTypeName(), itemName);
  }

  @SuppressWarnings("rawtypes")
  private Schema<?> resolveContainerSchema(
      Map<String, Schema> schemas,
      String wrapperName,
      ResponseTypeDescriptor descriptor,
      SupportedContainerDescriptor container) {
    if (container.shape() == ContainerShape.DIRECT_ARRAY) {
      return wrapperPayloadArraySchemaResolver.resolve(
          schemas, descriptor.dataRefName(), wrapperName, descriptor.payloadPropertyName());
    }

    if (container.shape() == ContainerShape.OBJECT_WITH_ITEM_ARRAY) {
      return componentContainerSchemaResolver.resolve(
          schemas, descriptor.dataRefName(), wrapperName, descriptor.payloadPropertyName());
    }

    return null;
  }

  @SuppressWarnings("rawtypes")
  private Schema<?> resolveItemArraySchema(
      Schema<?> containerSchema, SupportedContainerDescriptor container) {
    if (container.shape() == ContainerShape.DIRECT_ARRAY) {
      return containerSchema;
    }

    Map<String, Schema> properties = containerSchema.getProperties();
    if (properties == null) {
      return null;
    }

    return properties.get(container.itemPropertyName());
  }
}
