package io.github.blueprintplatform.openapi.generics.server.core.schema;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions;
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
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher();

        OpenAPI openApi = openApi(
                schema("PageCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")),
                schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

        enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDto");

        Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

        assertEquals("Page", wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
        assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.DATA_ITEM));
    }

    @Test
    @DisplayName("enrich -> should resolve referenced schema before extracting item type")
    void enrich_shouldResolveReferencedSchema() {
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher();

        Schema<?> pageRef = new Schema<>().$ref("#/components/schemas/PageCustomerDto");
        OpenAPI openApi = openApi(
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
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher();

        ComposedSchema pageComposed = new ComposedSchema();
        pageComposed.addAllOfItem(pageSchemaWithArrayContentRef("CustomerDto"));

        OpenAPI openApi = openApi(
                schema("PageCustomerDto", pageComposed),
                schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

        enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDto");

        Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

        assertEquals("Page", wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
        assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.DATA_ITEM));
    }

    @Test
    @DisplayName("enrich -> should support custom configured container names")
    void enrich_shouldSupportCustomContainerNames() {
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher(Set.of("Slice"));

        OpenAPI openApi = openApi(
                schema("SliceCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")),
                schema("ApiResponseSliceCustomerDto", new ObjectSchema()));

        enricher.enrich(openApi, "ApiResponseSliceCustomerDto", "SliceCustomerDto");

        Schema<?> wrapper = openApi.getComponents().getSchemas().get("ApiResponseSliceCustomerDto");

        assertEquals("Slice", wrapper.getExtensions().get(VendorExtensions.DATA_CONTAINER));
        assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.DATA_ITEM));
    }

    @Test
    @DisplayName("enrich -> should ignore unsupported container prefix")
    void enrich_shouldIgnoreUnsupportedContainerPrefix() {
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher();

        OpenAPI openApi = openApi(
                schema("ListCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")),
                schema("ServiceResponseListCustomerDto", new ObjectSchema()));

        enricher.enrich(openApi, "ServiceResponseListCustomerDto", "ListCustomerDto");

        Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponseListCustomerDto");

        assertNull(wrapper.getExtensions());
    }

    @Test
    @DisplayName("enrich -> should avoid false positive for partial container name match")
    void enrich_shouldAvoidFalsePositive_forPartialContainerMatch() {
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher(Set.of("Page"));

        OpenAPI openApi = openApi(
                schema("PagedCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")),
                schema("ServiceResponsePagedCustomerDto", new ObjectSchema()));

        enricher.enrich(openApi, "ServiceResponsePagedCustomerDto", "PagedCustomerDto");

        Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePagedCustomerDto");

        assertNull(wrapper.getExtensions());
    }

    @Test
    @DisplayName("enrich -> should ignore when wrapper schema is missing")
    void enrich_shouldIgnore_whenWrapperSchemaMissing() {
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher();

        OpenAPI openApi = openApi(schema("PageCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")));

        assertDoesNotThrow(() -> enricher.enrich(openApi, "MissingWrapper", "PageCustomerDto"));
    }

    @Test
    @DisplayName("enrich -> should ignore when data schema is missing")
    void enrich_shouldIgnore_whenDataSchemaMissing() {
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher();

        OpenAPI openApi = openApi(schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

        assertDoesNotThrow(
                () -> enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "MissingData"));
    }

    @Test
    @DisplayName("enrich -> should ignore when content property is missing")
    void enrich_shouldIgnore_whenContentPropertyMissing() {
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher();

        ObjectSchema pageSchema = new ObjectSchema();
        pageSchema.addProperty("totalElements", new Schema<>().type("integer"));

        OpenAPI openApi = openApi(
                schema("PageCustomerDto", pageSchema),
                schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

        enricher.enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDto");

        Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

        assertNull(wrapper.getExtensions());
    }

    @Test
    @DisplayName("enrich -> should extract item type from JsonSchema array content")
    void enrich_shouldExtractItemType_fromJsonSchemaArray() {
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher();

        JsonSchema content = new JsonSchema();
        content.setTypes(Set.of("array"));
        content.setItems(new Schema<>().$ref("#/components/schemas/CustomerDto"));

        ObjectSchema pageSchema = new ObjectSchema();
        pageSchema.addProperty("content", content);

        OpenAPI openApi = openApi(
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
        WrapperSchemaEnricher enricher = new WrapperSchemaEnricher();

        assertDoesNotThrow(() -> enricher.enrich(null, "Wrapper", "PageCustomerDto"));

        OpenAPI openApi = openApi(
                schema("PageCustomerDto", pageSchemaWithArrayContentRef("CustomerDto")),
                schema("ServiceResponsePageCustomerDto", new ObjectSchema()));

        assertDoesNotThrow(() -> enricher.enrich(openApi, null, "PageCustomerDto"));
        assertDoesNotThrow(() -> enricher.enrich(openApi, "ServiceResponsePageCustomerDto", null));
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