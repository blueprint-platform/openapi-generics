package io.github.blueprintplatform.openapi.generics.server.core.validation;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions.API_WRAPPER;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions.API_WRAPPER_DATATYPE;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions.DATA_CONTAINER;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions.DATA_ITEM;
import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: OpenApiContractGuard")
class OpenApiContractGuardTest {

    private final OpenApiContractGuard guard = new OpenApiContractGuard();

    @Test
    @DisplayName("validate -> should pass for valid simple wrapper")
    void validate_shouldPass_forValidSimpleWrapper() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");
        Schema<?> wrapper = wrapperWithAllOfPayload("data");
        wrapper.addExtension(API_WRAPPER, true);
        wrapper.addExtension(API_WRAPPER_DATATYPE, "CustomerDto");

        OpenAPI openApi = openApi(schema("ServiceResponseCustomerDto", wrapper));

        assertDoesNotThrow(() -> guard.validate(openApi, Set.of(descriptor)));
    }

    @Test
    @DisplayName("validate -> should pass for valid container wrapper")
    void validate_shouldPass_forValidContainerWrapper() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.container(ServiceResponse.class, "data", "Page", "CustomerDto");

        Schema<?> wrapper = wrapperWithAllOfPayload("data");
        wrapper.addExtension(API_WRAPPER, true);
        wrapper.addExtension(API_WRAPPER_DATATYPE, "PageCustomerDto");
        wrapper.addExtension(DATA_CONTAINER, "Page");
        wrapper.addExtension(DATA_ITEM, "CustomerDto");

        OpenAPI openApi = openApi(schema("ServiceResponsePageCustomerDto", wrapper));

        assertDoesNotThrow(() -> guard.validate(openApi, Set.of(descriptor)));
    }

    @Test
    @DisplayName("validate -> should pass when descriptors are empty")
    void validate_shouldPass_whenDescriptorsEmpty() {
        OpenAPI openApi = openApi();

        assertDoesNotThrow(() -> guard.validate(openApi, Set.of()));
    }

    @Test
    @DisplayName("validate -> should pass when descriptors are null")
    void validate_shouldPass_whenDescriptorsNull() {
        OpenAPI openApi = openApi();

        assertDoesNotThrow(() -> guard.validate(openApi, null));
    }

    @Test
    @DisplayName("validate -> should fail when components.schemas is missing")
    void validate_shouldFail_whenSchemasMissing() {
        OpenAPI openApi = new OpenAPI();

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> guard.validate(openApi, Set.of()));

        assertEquals("OpenAPI components.schemas is missing", ex.getMessage());
    }

    @Test
    @DisplayName("validate -> should fail when wrapper schema is missing")
    void validate_shouldFail_whenWrapperMissing() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

        OpenAPI openApi = openApi();

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> guard.validate(openApi, Set.of(descriptor)));

        assertTrue(ex.getMessage().contains("Missing required wrapper schema"));
        assertTrue(ex.getMessage().contains("ServiceResponseCustomerDto"));
    }

    @Test
    @DisplayName("validate -> should fail when wrapper extensions are missing")
    void validate_shouldFail_whenExtensionsMissing() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

        Schema<?> wrapper = wrapperWithAllOfPayload("data");
        OpenAPI openApi = openApi(schema("ServiceResponseCustomerDto", wrapper));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> guard.validate(openApi, Set.of(descriptor)));

        assertTrue(ex.getMessage().contains("missing required extensions"));
    }

    @Test
    @DisplayName("validate -> should fail when x-api-wrapper is invalid")
    void validate_shouldFail_whenApiWrapperInvalid() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

        Schema<?> wrapper = wrapperWithAllOfPayload("data");
        wrapper.addExtension(API_WRAPPER, false);
        wrapper.addExtension(API_WRAPPER_DATATYPE, "CustomerDto");

        OpenAPI openApi = openApi(schema("ServiceResponseCustomerDto", wrapper));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> guard.validate(openApi, Set.of(descriptor)));

        assertTrue(ex.getMessage().contains("invalid extension: " + API_WRAPPER));
    }

    @Test
    @DisplayName("validate -> should fail when x-api-wrapper-datatype is invalid")
    void validate_shouldFail_whenDatatypeInvalid() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

        Schema<?> wrapper = wrapperWithAllOfPayload("data");
        wrapper.addExtension(API_WRAPPER, true);
        wrapper.addExtension(API_WRAPPER_DATATYPE, "WrongDto");

        OpenAPI openApi = openApi(schema("ServiceResponseCustomerDto", wrapper));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> guard.validate(openApi, Set.of(descriptor)));

        assertTrue(ex.getMessage().contains("invalid extension: " + API_WRAPPER_DATATYPE));
    }

    @Test
    @DisplayName("validate -> should fail when payload property is missing in allOf wrapper")
    void validate_shouldFail_whenPayloadMissingInAllOfWrapper() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

        Schema<?> wrapper = wrapperWithAllOfPayload("payload");
        wrapper.addExtension(API_WRAPPER, true);
        wrapper.addExtension(API_WRAPPER_DATATYPE, "CustomerDto");
        OpenAPI openApi = openApi(schema("ServiceResponseCustomerDto", wrapper));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> guard.validate(openApi, Set.of(descriptor)));

        assertTrue(ex.getMessage().contains("must define 'data' property"));
    }

    @Test
    @DisplayName("validate -> should fail when payload property is missing in direct properties wrapper")
    void validate_shouldFail_whenPayloadMissingInDirectWrapper() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

        ObjectSchema wrapper = new ObjectSchema();
        wrapper.addProperty("payload", new Schema<>());
        wrapper.addExtension(API_WRAPPER, true);
        wrapper.addExtension(API_WRAPPER_DATATYPE, "CustomerDto");

        OpenAPI openApi = openApi(schema("ServiceResponseCustomerDto", wrapper));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> guard.validate(openApi, Set.of(descriptor)));

        assertTrue(ex.getMessage().contains("must define 'data' property"));
    }

    @Test
    @DisplayName("validate -> should fail when container extensions are missing")
    void validate_shouldFail_whenContainerExtensionsMissing() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.container(ServiceResponse.class, "data", "Page", "CustomerDto");

        Schema<?> wrapper = wrapperWithAllOfPayload("data");
        wrapper.addExtension(API_WRAPPER, true);
        wrapper.addExtension(API_WRAPPER_DATATYPE, "PageCustomerDto");

        OpenAPI openApi = openApi(schema("ServiceResponsePageCustomerDto", wrapper));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> guard.validate(openApi, Set.of(descriptor)));

        assertTrue(ex.getMessage().contains("invalid extension: " + DATA_CONTAINER));
    }

    @Test
    @DisplayName("validate -> should fail when x-data-container is invalid")
    void validate_shouldFail_whenContainerInvalid() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.container(ServiceResponse.class, "data", "Page", "CustomerDto");

        Schema<?> wrapper = wrapperWithAllOfPayload("data");
        wrapper.addExtension(API_WRAPPER, true);
        wrapper.addExtension(API_WRAPPER_DATATYPE, "PageCustomerDto");
        wrapper.addExtension(DATA_CONTAINER, "Slice");
        wrapper.addExtension(DATA_ITEM, "CustomerDto");

        OpenAPI openApi = openApi(schema("ServiceResponsePageCustomerDto", wrapper));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> guard.validate(openApi, Set.of(descriptor)));

        assertTrue(ex.getMessage().contains("invalid extension: " + DATA_CONTAINER));
    }

    @Test
    @DisplayName("validate -> should fail when x-data-item is invalid")
    void validate_shouldFail_whenItemInvalid() {
        ResponseTypeDescriptor descriptor =
                ResponseTypeDescriptor.container(ServiceResponse.class, "data", "Page", "CustomerDto");

        Schema<?> wrapper = wrapperWithAllOfPayload("data");
        wrapper.addExtension(API_WRAPPER, true);
        wrapper.addExtension(API_WRAPPER_DATATYPE, "PageCustomerDto");
        wrapper.addExtension(DATA_CONTAINER, "Page");
        wrapper.addExtension(DATA_ITEM, "OrderDto");

        OpenAPI openApi = openApi(schema("ServiceResponsePageCustomerDto", wrapper));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> guard.validate(openApi, Set.of(descriptor)));

        assertTrue(ex.getMessage().contains("invalid extension: " + DATA_ITEM));
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

    private Schema<?> wrapperWithAllOfPayload(String payloadPropertyName) {
        ObjectSchema payloadPart = new ObjectSchema();
        payloadPart.addProperty(payloadPropertyName, new Schema<>());

        ComposedSchema wrapper = new ComposedSchema();
        wrapper.addAllOfItem(new Schema<>().$ref("#/components/schemas/ServiceResponse"));
        wrapper.addAllOfItem(payloadPart);
        return wrapper;
    }

    private record NamedSchema(String name, Schema<?> schema) {}
}