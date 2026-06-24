package io.github.blueprintplatform.openapi.generics.server.core.schema;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.SupportedContainerType;
import io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ContentArrayItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.DirectArrayItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.ComponentContainerSchemaResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.WrapperPayloadArraySchemaResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ContainerSchemaRegistry;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ContainerSchemaStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ListContainerSchemaStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.PageContainerSchemaStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.SetContainerSchemaStrategy;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: WrapperSchemaEnricher")
class WrapperSchemaEnricherTest {

  @Test
  @DisplayName("enrich -> should add container extensions for default Page<T> schema")
  void enrich_shouldAddContainerExtensions_forPageSchema() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi =
        openApi(
            schema("PageCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")),
            schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", pageDescriptor("CustomerDto"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertContainerMetadata(wrapper, "Page", Page.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should support List<T> container")
  void enrich_shouldSupportListContainer() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ArraySchema listSchema = arraySchemaWithItemRef("CustomerDto");

    ObjectSchema wrapperSchema = new ObjectSchema();
    wrapperSchema.addProperty("data", listSchema);

    OpenAPI openApi =
        openApi(
            schema("ListCustomerDto", listSchema),
            schema("ServiceResponseListCustomerDto", wrapperSchema));

    enricher.enrich(openApi, "ServiceResponseListCustomerDto", listDescriptor("CustomerDto"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponseListCustomerDto");

    assertContainerMetadata(wrapper, "List", java.util.List.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should resolve allOf object-like schema")
  void enrich_shouldResolveAllOfSchema() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ComposedSchema pageComposed = new ComposedSchema();
    pageComposed.addAllOfItem(pageSchemaWithArrayContentRef("CustomerDto"));

    OpenAPI openApi =
        openApi(
            schema("PageCustomerDto", pageComposed),
            schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", pageDescriptor("CustomerDto"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertContainerMetadata(wrapper, "Page", Page.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should support custom container strategy")
  void enrich_shouldSupportCustomContainerStrategy() {
    ContainerSchemaStrategy sliceStrategy =
        new ContainerSchemaStrategy() {

          private final SupportedContainerType containerType =
              new SupportedContainerType(Slice.class, "Slice", "Slice");

          private final ComponentContainerSchemaResolver resolver =
              new ComponentContainerSchemaResolver();

          private final DirectArrayItemExtractor extractor = new DirectArrayItemExtractor();

          @Override
          public SupportedContainerType containerType() {
            return containerType;
          }

          @Override
          public ComponentContainerSchemaResolver resolver() {
            return resolver;
          }

          @Override
          public DirectArrayItemExtractor extractor() {
            return extractor;
          }
        };

    WrapperSchemaEnricher enricher =
        new WrapperSchemaEnricher(new ContainerSchemaRegistry(java.util.List.of(sliceStrategy)));

    ArraySchema sliceSchema = arraySchemaWithItemRef("CustomerDto");

    OpenAPI openApi =
        openApi(
            schema("SliceCustomerDto", sliceSchema),
            schema("ApiResponseSliceCustomerDto", new ObjectSchema()));

    enricher.enrich(
        openApi,
        "ApiResponseSliceCustomerDto",
        containerDescriptor(Slice.class, "Slice", "CustomerDto"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ApiResponseSliceCustomerDto");

    assertContainerMetadata(wrapper, "Slice", Slice.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should ignore unsupported container name")
  void enrich_shouldIgnoreUnsupportedContainerName() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ObjectSchema unsupportedSchema = new ObjectSchema();
    unsupportedSchema.addProperty("content", arraySchemaWithItemRef("CustomerDto"));

    OpenAPI openApi =
        openApi(
            schema("FooBarCustomerDto", unsupportedSchema),
            schema("ServiceResponseFooBarCustomerDto", new ObjectSchema()));

    enricher.enrich(
        openApi,
        "ServiceResponseFooBarCustomerDto",
        containerDescriptor(FooBar.class, "FooBar", "CustomerDto"));

    Schema<?> wrapper =
        openApi.getComponents().getSchemas().get("ServiceResponseFooBarCustomerDto");

    assertNull(wrapper.getExtensions());
  }

  @Test
  @DisplayName("enrich -> should ignore when wrapper schema is missing")
  void enrich_shouldIgnore_whenWrapperSchemaMissing() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi =
        openApi(schema("PageCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")));

    assertDoesNotThrow(
        () -> enricher.enrich(openApi, "MissingWrapper", pageDescriptor("CustomerDto")));
  }

  @Test
  @DisplayName("enrich -> should ignore when data schema is missing")
  void enrich_shouldIgnore_whenDataSchemaMissing() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi = openApi(schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    assertDoesNotThrow(
        () ->
            enricher.enrich(
                openApi,
                "ServiceResponsePageCustomerDto",
                containerDescriptor(MissingData.class, "MissingData", "CustomerDto")));
  }

  @Test
  @DisplayName("enrich -> should ignore when content property is missing")
  void enrich_shouldIgnore_whenContentPropertyMissing() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ObjectSchema pageSchema = new ObjectSchema();
    pageSchema.addProperty("totalElements", new Schema<>().type("integer"));

    OpenAPI openApi =
        openApi(
            schema("PageCustomerDto", pageSchema),
            schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", pageDescriptor("CustomerDto"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertNull(wrapper.getExtensions());
  }

  @Test
  @DisplayName("enrich -> should extract item type from JsonSchema array content")
  void enrich_shouldExtractItemType_fromJsonSchemaArray() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    JsonSchema content = new JsonSchema();
    content.setTypes(Set.of("array"));
    content.setItems(new Schema<>().$ref("#/components/schemas/CustomerDto"));

    ObjectSchema pageSchema = new ObjectSchema();
    pageSchema.addProperty("content", content);

    OpenAPI openApi =
        openApi(
            schema("PageCustomerDto", pageSchema),
            schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", pageDescriptor("CustomerDto"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertContainerMetadata(wrapper, "Page", Page.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should do nothing for null inputs")
  void enrich_shouldDoNothing_forNullInputs() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    assertDoesNotThrow(() -> enricher.enrich(null, "Wrapper", pageDescriptor("CustomerDto")));

    OpenAPI openApi =
        openApi(
            schema("PageCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")),
            schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    assertDoesNotThrow(() -> enricher.enrich(openApi, null, pageDescriptor("CustomerDto")));
    assertDoesNotThrow(() -> enricher.enrich(openApi, "ServiceResponsePageCustomerDto", null));
    assertDoesNotThrow(
        () ->
            enricher.enrich(
                openApi,
                "ServiceResponsePageCustomerDto",
                ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto")));
  }

  @Test
  @DisplayName("enrich -> should do nothing when components are null")
  void enrich_shouldDoNothing_whenComponentsNull() {
    WrapperSchemaEnricher enricher = defaultEnricher();
    OpenAPI openApi = new OpenAPI();

    assertDoesNotThrow(
        () ->
            enricher.enrich(
                openApi, "ServiceResponsePageCustomerDto", pageDescriptor("CustomerDto")));
  }

  @Test
  @DisplayName("enrich -> should do nothing when schemas map is empty")
  void enrich_shouldDoNothing_whenSchemasEmpty() {
    WrapperSchemaEnricher enricher = defaultEnricher();
    OpenAPI openApi = new OpenAPI().components(new Components());

    assertDoesNotThrow(
        () ->
            enricher.enrich(
                openApi, "ServiceResponsePageCustomerDto", pageDescriptor("CustomerDto")));
  }

  @Test
  @DisplayName("constructor -> should work with custom strategy registry")
  void constructor_shouldWorkWithCustomStrategyRegistry() {
    ContainerSchemaStrategy customListStrategy =
        new ContainerSchemaStrategy() {

          private final SupportedContainerType containerType =
              new SupportedContainerType(CustomList.class, "CustomList", "CustomList");

          private final ComponentContainerSchemaResolver resolver =
              new ComponentContainerSchemaResolver();

          private final DirectArrayItemExtractor extractor = new DirectArrayItemExtractor();

          @Override
          public SupportedContainerType containerType() {
            return containerType;
          }

          @Override
          public ComponentContainerSchemaResolver resolver() {
            return resolver;
          }

          @Override
          public DirectArrayItemExtractor extractor() {
            return extractor;
          }
        };

    WrapperSchemaEnricher enricher =
        new WrapperSchemaEnricher(
            new ContainerSchemaRegistry(java.util.List.of(customListStrategy)));

    ArraySchema customListSchema = arraySchemaWithItemRef("CustomerDto");

    OpenAPI openApi =
        openApi(
            schema("CustomListCustomerDto", customListSchema),
            schema("ServiceResponseCustomListCustomerDto", new ObjectSchema()));

    enricher.enrich(
        openApi,
        "ServiceResponseCustomListCustomerDto",
        containerDescriptor(CustomList.class, "CustomList", "CustomerDto"));

    Schema<?> wrapper =
        openApi.getComponents().getSchemas().get("ServiceResponseCustomListCustomerDto");

    assertContainerMetadata(wrapper, "CustomList", CustomList.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should use custom payload property for array container")
  void enrich_shouldUseCustomPayloadProperty_forArrayContainer() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ArraySchema listSchema = arraySchemaWithItemRef("CustomerDto");

    ObjectSchema wrapperSchema = new ObjectSchema();
    wrapperSchema.addProperty("payload", listSchema);

    OpenAPI openApi =
        openApi(
            schema("ListCustomerDto", listSchema),
            schema("ApiResponseListCustomerDto", wrapperSchema));

    enricher.enrich(
        openApi, "ApiResponseListCustomerDto", listDescriptor("CustomerDto", "payload"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ApiResponseListCustomerDto");

    assertContainerMetadata(wrapper, "List", java.util.List.class.getName(), "CustomerDto");
  }

  @Test
  @DisplayName("enrich -> should support Set<T> container")
  void enrich_shouldSupportSetContainer() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ArraySchema setSchema = arraySchemaWithItemRef("CustomerDto");

    ObjectSchema wrapperSchema = new ObjectSchema();
    wrapperSchema.addProperty("data", setSchema);

    OpenAPI openApi =
        openApi(
            schema("SetCustomerDto", setSchema),
            schema("ServiceResponseSetCustomerDto", wrapperSchema));

    enricher.enrich(openApi, "ServiceResponseSetCustomerDto", setDescriptor("CustomerDto"));

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponseSetCustomerDto");

    assertContainerMetadata(wrapper, "Set", java.util.Set.class.getName(), "CustomerDto");
  }

  private WrapperSchemaEnricher defaultEnricher() {
    return new WrapperSchemaEnricher(
        new ContainerSchemaRegistry(
            java.util.List.of(
                new PageContainerSchemaStrategy(
                    new ComponentContainerSchemaResolver(), new ContentArrayItemExtractor()),
                new ListContainerSchemaStrategy(
                    new WrapperPayloadArraySchemaResolver(), new DirectArrayItemExtractor()),
                new SetContainerSchemaStrategy(
                    new WrapperPayloadArraySchemaResolver(), new DirectArrayItemExtractor()))));
  }

  private ResponseTypeDescriptor pageDescriptor(String itemRefName) {
    return containerDescriptor(Page.class, "Page", itemRefName);
  }

  private ResponseTypeDescriptor pageAliasDescriptor(String itemRefName) {
    SupportedContainerType containerType =
        new SupportedContainerType(Page.class, "PageAlias", "Page");

    return ResponseTypeDescriptor.container(
        ServiceResponse.class, "data", containerType, itemRefName);
  }

  private ResponseTypeDescriptor listDescriptor(String itemRefName) {
    return listDescriptor(itemRefName, "data");
  }

  private ResponseTypeDescriptor listDescriptor(String itemRefName, String payloadPropertyName) {
    return containerDescriptor(java.util.List.class, "List", itemRefName, payloadPropertyName);
  }

  private ResponseTypeDescriptor setDescriptor(String itemRefName) {
    return containerDescriptor(java.util.Set.class, "Set", itemRefName);
  }

  private ResponseTypeDescriptor containerDescriptor(
      Class<?> containerClass, String containerName, String itemRefName) {
    return containerDescriptor(containerClass, containerName, itemRefName, "data");
  }

  private ResponseTypeDescriptor containerDescriptor(
      Class<?> containerClass,
      String containerName,
      String itemRefName,
      String payloadPropertyName) {
    SupportedContainerType containerType =
        new SupportedContainerType(containerClass, containerName, containerName);

    return ResponseTypeDescriptor.container(
        ServiceResponse.class, payloadPropertyName, containerType, itemRefName);
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

  private ObjectSchema pageSchemaWithArrayContentRef(String itemRefName) {
    ObjectSchema pageSchema = new ObjectSchema();
    pageSchema.addProperty("content", arraySchemaWithItemRef(itemRefName));
    return pageSchema;
  }

  private ArraySchema arraySchemaWithItemRef(String itemRefName) {
    ArraySchema schema = new ArraySchema();
    schema.setItems(new Schema<>().$ref("#/components/schemas/" + itemRefName));
    return schema;
  }

  private record NamedSchema(String name, Schema<?> schema) {}

  private static final class ServiceResponse {}

  private static final class Slice {}

  private static final class FooBar {}

  private static final class MissingData {}

  private static final class CustomList {}
}
