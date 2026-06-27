package io.github.blueprintplatform.openapi.generics.server.core.schema.enrichment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extraction.ArrayItemReferenceExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolution.ComponentContainerSchemaResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolution.WrapperPayloadArraySchemaResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: WrapperSchemaEnricher")
class WrapperSchemaEnricherTest {

  @Test
  @DisplayName("enrich -> should do nothing when openApi is null")
  void enrich_shouldDoNothing_whenOpenApiIsNull() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    assertDoesNotThrow(
        () ->
            enricher.enrich(null, "ServiceResponsePageCustomerDto", pageDescriptor("CustomerDto")));
  }

  @Test
  @DisplayName("enrich -> should do nothing when wrapper name is null")
  void enrich_shouldDoNothing_whenWrapperNameIsNull() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi =
        openApi(
            schema("PageCustomerDto", objectContainerSchema("content", "CustomerDto")),
            schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    assertDoesNotThrow(() -> enricher.enrich(openApi, null, pageDescriptor("CustomerDto")));
  }

  @Test
  @DisplayName("enrich -> should do nothing when descriptor is null")
  void enrich_shouldDoNothing_whenDescriptorIsNull() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi =
        openApi(
            schema("PageCustomerDto", objectContainerSchema("content", "CustomerDto")),
            schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    assertDoesNotThrow(() -> enricher.enrich(openApi, "ServiceResponsePageCustomerDto", null));
  }

  @Test
  @DisplayName("enrich -> should ignore when wrapper schema is missing")
  void enrich_shouldIgnore_whenWrapperSchemaMissing() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi =
        openApi(schema("PageCustomerDto", objectContainerSchema("content", "CustomerDto")));

    assertDoesNotThrow(
        () -> enricher.enrich(openApi, "MissingWrapper", pageDescriptor("CustomerDto")));
  }

  @Test
  @DisplayName("enrich -> should ignore when direct array payload property is missing")
  void enrich_shouldIgnore_whenDirectArrayPayloadPropertyMissing() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ArraySchema listSchema = arraySchemaWithItemRef("CustomerDto");

    ObjectSchema wrapper = new ObjectSchema();
    wrapper.addProperty("payload", listSchema);

    OpenAPI openApi =
        openApi(
            schema("ListCustomerDto", listSchema),
            schema("ServiceResponseListCustomerDto", wrapper));

    enricher.enrich(openApi, "ServiceResponseListCustomerDto", listDescriptor("CustomerDto"));

    Schema<?> result = openApi.getComponents().getSchemas().get("ServiceResponseListCustomerDto");

    assertNull(result.getExtensions());
  }

  @Test
  @DisplayName("enrich -> should ignore non-container descriptor")
  void enrich_shouldIgnoreNonContainerDescriptor() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi = openApi(schema("ServiceResponseCustomerDto", new ObjectSchema()));

    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    assertDoesNotThrow(() -> enricher.enrich(openApi, "ServiceResponseCustomerDto", descriptor));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponseCustomerDto");

    assertNull(wrapper.getExtensions());
  }

  @Test
  @DisplayName("enrich -> should add metadata for object container")
  void enrich_shouldAddMetadata_forObjectContainer() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi =
        openApi(
            schema("PageCustomerDto", objectContainerSchema("content", "CustomerDto")),
            schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", pageDescriptor("CustomerDto"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertContainerMetadata(wrapper, "Page", Page.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should add metadata for direct array container")
  void enrich_shouldAddMetadata_forDirectArrayContainer() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ArraySchema arraySchema = arraySchemaWithItemRef("CustomerDto");

    ObjectSchema wrapperSchema = new ObjectSchema();
    wrapperSchema.addProperty("data", arraySchema);

    OpenAPI openApi =
        openApi(
            schema("ListCustomerDto", arraySchema),
            schema("ServiceResponseListCustomerDto", wrapperSchema));

    enricher.enrich(openApi, "ServiceResponseListCustomerDto", listDescriptor("CustomerDto"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponseListCustomerDto");

    assertContainerMetadata(wrapper, "List", java.util.List.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should add metadata for configured object container")
  void enrich_shouldAddMetadata_forConfiguredObjectContainer() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi =
        openApi(
            schema("PagingCustomerDto", objectContainerSchema("items", "CustomerDto")),
            schema("ServiceResponsePagingCustomerDto", new ObjectSchema()));

    enricher.enrich(
        openApi,
        "ServiceResponsePagingCustomerDto",
        objectContainerDescriptor(Paging.class, "Paging", "items", "CustomerDto"));

    Schema<?> wrapper =
        openApi.getComponents().getSchemas().get("ServiceResponsePagingCustomerDto");

    assertContainerMetadata(wrapper, "Paging", Paging.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should ignore when metadata cannot be resolved")
  void enrich_shouldIgnore_whenMetadataCannotBeResolved() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi =
        openApi(
            schema("PageCustomerDto", new ObjectSchema()),
            schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    assertDoesNotThrow(
        () ->
            enricher.enrich(
                openApi, "ServiceResponsePageCustomerDto", pageDescriptor("CustomerDto")));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertNull(wrapper.getExtensions());
  }

  @Test
  @DisplayName("enrich -> should resolve object container through component ref")
  void enrich_shouldResolveObjectContainer_throughComponentRef() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    Schema<?> pageRef = new Schema<>().$ref("#/components/schemas/PageCustomerDto");

    OpenAPI openApi =
        openApi(
            schema("PageCustomerDto", objectContainerSchema("content", "CustomerDto")),
            schema("ServiceResponsePageCustomerDto", pageRef));

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", pageDescriptor("CustomerDto"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertContainerMetadata(wrapper, "Page", Page.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should ignore when array item is not component ref")
  void enrich_shouldIgnore_whenArrayItemIsNotComponentRef() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ArraySchema arraySchema = new ArraySchema();
    arraySchema.setItems(new Schema<>().type("string"));

    ObjectSchema wrapperSchema = new ObjectSchema();
    wrapperSchema.addProperty("data", arraySchema);

    OpenAPI openApi =
        openApi(
            schema("ListString", arraySchema), schema("ServiceResponseListString", wrapperSchema));

    enricher.enrich(openApi, "ServiceResponseListString", listDescriptor("String"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponseListString");

    assertNull(wrapper.getExtensions());
  }

  @Test
  @DisplayName("enrich -> should ignore when configured item property does not exist")
  void enrich_shouldIgnore_whenConfiguredItemPropertyDoesNotExist() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi =
        openApi(
            schema("PagingCustomerDto", objectContainerSchema("content", "CustomerDto")),
            schema("ServiceResponsePagingCustomerDto", new ObjectSchema()));

    enricher.enrich(
        openApi,
        "ServiceResponsePagingCustomerDto",
        objectContainerDescriptor(Paging.class, "Paging", "items", "CustomerDto"));

    Schema<?> wrapper =
        openApi.getComponents().getSchemas().get("ServiceResponsePagingCustomerDto");

    assertNull(wrapper.getExtensions());
  }

  private WrapperSchemaEnricher defaultEnricher() {
    return new WrapperSchemaEnricher(
        new ContainerSchemaMetadataResolver(
            new WrapperPayloadArraySchemaResolver(),
            new ComponentContainerSchemaResolver(),
            new ArrayItemReferenceExtractor()));
  }

  private ResponseTypeDescriptor pageDescriptor(String itemRefName) {
    return objectContainerDescriptor(Page.class, "Page", "content", itemRefName);
  }

  private ResponseTypeDescriptor listDescriptor(String itemRefName) {
    SupportedContainerDescriptor container =
        new SupportedContainerDescriptor(
            java.util.List.class,
            "List",
            "List",
            ContainerShape.DIRECT_ARRAY,
            null,
            ContainerSource.BUILT_IN,
            ContainerMatchMode.ASSIGNABLE);

    return ResponseTypeDescriptor.container(ServiceResponse.class, "data", container, itemRefName);
  }

  private ResponseTypeDescriptor objectContainerDescriptor(
      Class<?> containerClass, String containerName, String itemPropertyName, String itemRefName) {
    SupportedContainerDescriptor container =
        new SupportedContainerDescriptor(
            containerClass,
            containerName,
            containerName,
            ContainerShape.OBJECT_WITH_ITEM_ARRAY,
            itemPropertyName,
            ContainerSource.CONFIGURED,
            ContainerMatchMode.EXACT);

    return ResponseTypeDescriptor.container(ServiceResponse.class, "data", container, itemRefName);
  }

  private ObjectSchema objectContainerSchema(String itemPropertyName, String itemRefName) {
    ObjectSchema schema = new ObjectSchema();
    schema.addProperty(itemPropertyName, arraySchemaWithItemRef(itemRefName));
    return schema;
  }

  private ArraySchema arraySchemaWithItemRef(String itemRefName) {
    ArraySchema schema = new ArraySchema();
    schema.setItems(new Schema<>().$ref("#/components/schemas/" + itemRefName));
    return schema;
  }

  private void assertContainerMetadata(
      Schema<?> wrapper, String containerName, String containerTypeName, String itemName) {
    assertEquals(containerName, wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
    assertEquals(
        containerTypeName, wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER_TYPE));
    assertEquals(itemName, wrapper.getExtensions().get(VendorExtensions.DATA_ITEM));
  }

  private OpenAPI openApi(NamedSchema... namedSchemas) {
    Map<String, Schema> schemas = new LinkedHashMap<>();

    for (NamedSchema namedSchema : namedSchemas) {
      schemas.put(namedSchema.name, namedSchema.schema);
    }

    return new OpenAPI().components(new Components().schemas(schemas));
  }

  private NamedSchema schema(String name, Schema<?> schema) {
    schema.setName(name);
    return new NamedSchema(name, schema);
  }

  private record NamedSchema(String name, Schema<?> schema) {}

  private static final class ServiceResponse {}

  private static final class Paging<T> {}
}
