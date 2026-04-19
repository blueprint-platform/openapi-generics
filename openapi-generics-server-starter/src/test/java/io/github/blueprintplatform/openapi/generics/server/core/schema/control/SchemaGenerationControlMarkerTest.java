package io.github.blueprintplatform.openapi.generics.server.core.schema.control;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Sort;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SchemaGenerationControlMarker")
class SchemaGenerationControlMarkerTest {

    private final SchemaGenerationControlMarker marker = new SchemaGenerationControlMarker();

    @Test
    @DisplayName("mark -> should ignore default envelope and internal contract schemas")
    void mark_shouldIgnoreDefaultEnvelopeAndInternalSchemas() {
        OpenAPI openApi =
                openApi(
                        schema("ServiceResponse", new ObjectSchema()),
                        schema("Meta", new ObjectSchema()),
                        schema("Sort", new ObjectSchema()),
                        schema("ServiceResponseCustomerDto", wrapperWithProperties(Map.of("data", ref("CustomerDto")))),
                        schema("CustomerDto", new ObjectSchema()));

        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

        marker.mark(openApi, Set.of(descriptor));

        assertIgnored(openApi, "ServiceResponse");
        assertIgnored(openApi, "Meta");
        assertIgnored(openApi, "Sort");
        assertNotIgnored(openApi, "CustomerDto");
    }

    @Test
    @DisplayName("mark -> should ignore container schema for default envelope container response")
    void mark_shouldIgnoreContainerSchema_forContainerResponse() {
        OpenAPI openApi =
                openApi(
                        schema("ServiceResponse", new ObjectSchema()),
                        schema("Meta", new ObjectSchema()),
                        schema("Sort", new ObjectSchema()),
                        schema(
                                "ServiceResponsePageCustomerDto",
                                wrapperWithProperties(Map.of("data", ref("PageCustomerDto")))),
                        schema("PageCustomerDto", new ObjectSchema()));

        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.container(ServiceResponse.class, "data", "Page", "CustomerDto");

        marker.mark(openApi, Set.of(descriptor));

        assertIgnored(openApi, "ServiceResponse");
        assertIgnored(openApi, "Meta");
        assertIgnored(openApi, "Sort");
        assertIgnored(openApi, "PageCustomerDto");
    }

    @Test
    @DisplayName("mark -> should ignore nested non-payload referenced schemas")
    void mark_shouldIgnoreNestedNonPayloadReferencedSchemas() {
        OpenAPI openApi =
                openApi(
                        schema("ApiResponse", new ObjectSchema()),
                        schema(
                                "ApiResponseCustomerDto",
                                wrapperWithProperties(
                                        Map.of(
                                                "data", ref("CustomerDto"),
                                                "errors", arrayOfRefs("ApiError"),
                                                "meta", ref("CustomMeta")))),
                        schema("CustomerDto", new ObjectSchema()),
                        schema("ApiError", new ObjectSchema()),
                        schema("CustomMeta", new ObjectSchema()));

        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ApiResponse.class, "data", "CustomerDto");

        marker.mark(openApi, Set.of(descriptor));

        assertIgnored(openApi, "ApiResponse");
        assertIgnored(openApi, "ApiError");
        assertIgnored(openApi, "CustomMeta");
        assertNotIgnored(openApi, "CustomerDto");
    }

    @Test
    @DisplayName("mark -> should not ignore Meta and Sort for custom envelope")
    void mark_shouldNotIgnoreMetaAndSort_forCustomEnvelope() {
        OpenAPI openApi =
                openApi(
                        schema("ApiResponse", new ObjectSchema()),
                        schema("Meta", new ObjectSchema()),
                        schema("Sort", new ObjectSchema()),
                        schema("ApiResponseCustomerDto", wrapperWithProperties(Map.of("payload", ref("CustomerDto")))),
                        schema("CustomerDto", new ObjectSchema()));

        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ApiResponse.class, "payload", "CustomerDto");

        marker.mark(openApi, Set.of(descriptor));

        assertIgnored(openApi, "ApiResponse");
        assertNotIgnored(openApi, "Meta");
        assertNotIgnored(openApi, "Sort");
    }

    @Test
    @DisplayName("mark -> should do nothing when wrapper schema is missing")
    void mark_shouldDoNothing_whenWrapperSchemaMissing() {
        OpenAPI openApi =
                openApi(
                        schema("ServiceResponse", new ObjectSchema()),
                        schema("Meta", new ObjectSchema()),
                        schema("Sort", new ObjectSchema()));

        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

        assertDoesNotThrow(() -> marker.mark(openApi, Set.of(descriptor)));

        assertIgnored(openApi, "ServiceResponse");
        assertIgnored(openApi, "Meta");
        assertIgnored(openApi, "Sort");
    }

    @Test
    @DisplayName("mark -> should do nothing for null inputs")
    void mark_shouldDoNothing_forNullInputs() {
        assertDoesNotThrow(() -> marker.mark(null, Set.of()));
        assertDoesNotThrow(() -> marker.mark(new OpenAPI(), null));
        assertDoesNotThrow(() -> marker.mark(new OpenAPI(), Set.of()));
    }

    @Test
    @DisplayName("mark -> should ignore nested array item references")
    void mark_shouldIgnoreNestedArrayItemReferences() {
        OpenAPI openApi =
                openApi(
                        schema("ApiResponse", new ObjectSchema()),
                        schema(
                                "ApiResponseCustomerDto",
                                wrapperWithProperties(
                                        Map.of(
                                                "payload", ref("CustomerDto"),
                                                "warnings", arrayOfRefs("WarningItem")))),
                        schema("CustomerDto", new ObjectSchema()),
                        schema("WarningItem", new ObjectSchema()));

        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ApiResponse.class, "payload", "CustomerDto");

        marker.mark(openApi, Set.of(descriptor));

        assertIgnored(openApi, "ApiResponse");
        assertIgnored(openApi, "WarningItem");
        assertNotIgnored(openApi, "CustomerDto");
    }

    private OpenAPI openApi(NamedSchema... namedSchemas) {
        Map<String, Schema> schemas = new LinkedHashMap<>();
        for (NamedSchema namedSchema : namedSchemas) {
            schemas.put(namedSchema.name(), namedSchema.schema());
        }
        return new OpenAPI().components(new Components().schemas(schemas));
    }

    private NamedSchema schema(String name, Schema<?> schema) {
        schema.setName(name);
        return new NamedSchema(name, schema);
    }

    private Schema<?> ref(String schemaName) {
        return new Schema<>().$ref("#/components/schemas/" + schemaName);
    }

    private Schema<?> arrayOfRefs(String schemaName) {
        ArraySchema arraySchema = new ArraySchema();
        arraySchema.setItems(ref(schemaName));
        return arraySchema;
    }

    private Schema<?> wrapperWithProperties(Map<String, Schema> properties) {
        ObjectSchema wrapper = new ObjectSchema();
        properties.forEach(wrapper::addProperty);
        return wrapper;
    }

    private void assertIgnored(OpenAPI openApi, String schemaName) {
        Schema<?> schema = openApi.getComponents().getSchemas().get(schemaName);
        assertNotNull(schema);
        assertNotNull(schema.getExtensions());
        assertEquals(Boolean.TRUE, schema.getExtensions().get(VendorExtensions.IGNORE_MODEL));
    }

    private void assertNotIgnored(OpenAPI openApi, String schemaName) {
        Schema<?> schema = openApi.getComponents().getSchemas().get(schemaName);
        assertNotNull(schema);
        if (schema.getExtensions() != null) {
            assertNotEquals(Boolean.TRUE, schema.getExtensions().get(VendorExtensions.IGNORE_MODEL));
        }
    }

    static final class ApiResponse<T> {
        T payload;
    }

    private record NamedSchema(String name, Schema<?> schema) {}
}