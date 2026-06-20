package io.github.blueprintplatform.openapi.generics.server.core.schema;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.ListItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.extractor.PageItemExtractor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.DirectSchemaResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.resolver.WrapperEmbeddedArrayResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ContainerSchemaRegistry;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ContainerSchemaStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.ListContainerSchemaStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.schema.strategy.PageContainerSchemaStrategy;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.List;
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

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDto");

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertEquals("Page", wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
    assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.DATA_ITEM));
  }

  @Test
  @DisplayName("enrich -> should support List<T> container")
  void enrich_shouldSupportListContainer() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ArraySchema listSchema = new ArraySchema();
    listSchema.setItems(new Schema<>().$ref("#/components/schemas/CustomerDto"));

    ObjectSchema wrapperSchema = new ObjectSchema();
    wrapperSchema.addProperty("data", listSchema);

    OpenAPI openApi =
            openApi(
                    schema("ListCustomerDto", listSchema),
                    schema("ServiceResponseListCustomerDto", wrapperSchema));

    enricher.enrich(openApi, "ServiceResponseListCustomerDto", "ListCustomerDto");

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponseListCustomerDto");

    assertEquals("List", wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
    assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.DATA_ITEM));
  }

  @Test
  @DisplayName("enrich -> should resolve referenced schema before extracting item type")
  void enrich_shouldResolveReferencedSchema() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    Schema<?> pageRef = new Schema<>().$ref("#/components/schemas/PageCustomerDto");

    OpenAPI openApi =
            openApi(
                    schema("PageCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")),
                    schema("ServiceResponsePageCustomerDto", new ObjectSchema()),
                    schema("PageCustomerDtoAlias", pageRef));

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDtoAlias");

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertEquals("Page", wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
    assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.DATA_ITEM));
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

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDto");

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertEquals("Page", wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
    assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.DATA_ITEM));
  }

  @Test
  @DisplayName("enrich -> should support custom container strategy")
  void enrich_shouldSupportCustomContainerStrategy() {
    ContainerSchemaStrategy sliceStrategy =
            new ContainerSchemaStrategy() {

              private final DirectSchemaResolver resolver = new DirectSchemaResolver();
              private final ListItemExtractor extractor = new ListItemExtractor();

              @Override
              public String containerName() {
                return "Slice";
              }

              @Override
              public DirectSchemaResolver resolver() {
                return resolver;
              }

              @Override
              public ListItemExtractor extractor() {
                return extractor;
              }
            };

    WrapperSchemaEnricher enricher =
            new WrapperSchemaEnricher(new ContainerSchemaRegistry(List.of(sliceStrategy)));

    ArraySchema sliceSchema = new ArraySchema();
    sliceSchema.setItems(new Schema<>().$ref("#/components/schemas/CustomerDto"));

    OpenAPI openApi =
            openApi(
                    schema("SliceCustomerDto", sliceSchema),
                    schema("ApiResponseSliceCustomerDto", new ObjectSchema()));

    enricher.enrich(openApi, "ApiResponseSliceCustomerDto", "SliceCustomerDto");

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ApiResponseSliceCustomerDto");

    assertEquals("Slice", wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
    assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.DATA_ITEM));
  }

  @Test
  @DisplayName("enrich -> should ignore unsupported container prefix")
  void enrich_shouldIgnoreUnsupportedContainerPrefix() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ObjectSchema unsupportedSchema = new ObjectSchema();
    unsupportedSchema.addProperty(
            "content", new ArraySchema().items(new Schema<>().$ref("#/components/schemas/CustomerDto")));

    OpenAPI openApi =
            openApi(
                    schema("FooBarCustomerDto", unsupportedSchema),
                    schema("ServiceResponseFooBarCustomerDto", new ObjectSchema()));

    enricher.enrich(openApi, "ServiceResponseFooBarCustomerDto", "FooBarCustomerDto");

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponseFooBarCustomerDto");

    assertNull(wrapper.getExtensions());
  }

  @Test
  @DisplayName("enrich -> should avoid false positive for partial container name match")
  void enrich_shouldAvoidFalsePositive_forPartialContainerMatch() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    ArraySchema content = new ArraySchema();
    content.setItems(new Schema<>().$ref("#/components/schemas/CustomerDto"));

    ObjectSchema pageSchema = new ObjectSchema();
    pageSchema.addProperty("content", content);

    OpenAPI openApi =
            openApi(
                    schema("PagedCustomerDto", pageSchema),
                    schema("ServiceResponsePagedCustomerDto", new ObjectSchema()));

    enricher.enrich(openApi, "ServiceResponsePagedCustomerDto", "PagedCustomerDto");

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePagedCustomerDto");

    assertNull(wrapper.getExtensions());
  }

  @Test
  @DisplayName("enrich -> should ignore when wrapper schema is missing")
  void enrich_shouldIgnore_whenWrapperSchemaMissing() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi = openApi(schema("PageCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")));

    assertDoesNotThrow(() -> enricher.enrich(openApi, "MissingWrapper", "PageCustomerDto"));
  }

  @Test
  @DisplayName("enrich -> should ignore when data schema is missing")
  void enrich_shouldIgnore_whenDataSchemaMissing() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    OpenAPI openApi = openApi(schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    assertDoesNotThrow(() -> enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "MissingData"));
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

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDto");

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

    enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDto");

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertEquals("Page", wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
    assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.DATA_ITEM));
  }

  @Test
  @DisplayName("enrich -> should do nothing for null inputs")
  void enrich_shouldDoNothing_forNullInputs() {
    WrapperSchemaEnricher enricher = defaultEnricher();

    assertDoesNotThrow(() -> enricher.enrich(null, "Wrapper", "PageCustomerDto"));

    OpenAPI openApi =
            openApi(
                    schema("PageCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")),
                    schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

    assertDoesNotThrow(() -> enricher.enrich(openApi, null, "PageCustomerDto"));
    assertDoesNotThrow(() -> enricher.enrich(openApi, "ServiceResponsePageCustomerDto", null));
  }

  @Test
  @DisplayName("enrich -> should do nothing when components are null")
  void enrich_shouldDoNothing_whenComponentsNull() {
    WrapperSchemaEnricher enricher = defaultEnricher();
    OpenAPI openApi = new OpenAPI();

    assertDoesNotThrow(
            () -> enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDto"));
  }

  @Test
  @DisplayName("enrich -> should do nothing when schemas map is empty")
  void enrich_shouldDoNothing_whenSchemasEmpty() {
    WrapperSchemaEnricher enricher = defaultEnricher();
    OpenAPI openApi = new OpenAPI().components(new Components());

    assertDoesNotThrow(
            () -> enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDto"));
  }

  @Test
  @DisplayName("constructor -> should work with custom strategy registry")
  void constructor_shouldWorkWithCustomStrategyRegistry() {
    ContainerSchemaStrategy customListStrategy =
            new ContainerSchemaStrategy() {

              private final DirectSchemaResolver resolver = new DirectSchemaResolver();
              private final ListItemExtractor extractor = new ListItemExtractor();

              @Override
              public String containerName() {
                return "CustomList";
              }

              @Override
              public DirectSchemaResolver resolver() {
                return resolver;
              }

              @Override
              public ListItemExtractor extractor() {
                return extractor;
              }
            };

    WrapperSchemaEnricher enricher =
            new WrapperSchemaEnricher(new ContainerSchemaRegistry(List.of(customListStrategy)));

    ArraySchema listSchema = new ArraySchema();
    listSchema.setItems(new Schema<>().$ref("#/components/schemas/CustomerDto"));

    OpenAPI openApi =
            openApi(
                    schema("CustomListCustomerDto", listSchema),
                    schema("ServiceResponseCustomListCustomerDto", new ObjectSchema()));

    enricher.enrich(openApi, "ServiceResponseCustomListCustomerDto", "CustomListCustomerDto");

    Schema<?> wrapper =
            openApi.getComponents().getSchemas().get("ServiceResponseCustomListCustomerDto");

    assertEquals("CustomList", wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
  }

  private WrapperSchemaEnricher defaultEnricher() {
    return new WrapperSchemaEnricher(
            new ContainerSchemaRegistry(
                    List.of(
                            new PageContainerSchemaStrategy(new DirectSchemaResolver(), new PageItemExtractor()),
                            new ListContainerSchemaStrategy(
                                    new WrapperEmbeddedArrayResolver(), new ListItemExtractor()))));
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
    ArraySchema content = new ArraySchema();
    content.setItems(new Schema<>().$ref("#/components/schemas/" + itemRefName));

    ObjectSchema pageSchema = new ObjectSchema();
    pageSchema.addProperty("content", content);

    return pageSchema;
  }

  private record NamedSchema(String name, Schema<?> schema) {}
}