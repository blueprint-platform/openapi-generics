package io.github.blueprintplatform.openapi.generics.codegen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;

@Tag("unit")
@DisplayName("Unit Test: ExternalImportResolver")
class ExternalImportResolverTest {

    @Test
    @DisplayName("apply -> does nothing when model is not a wrapper")
    void apply_shouldDoNothing_whenNotWrapper() {
        ExternalModelRegistry registry = new ExternalModelRegistry();
        ExternalImportResolver resolver = new ExternalImportResolver(registry);

        CodegenModel model = new CodegenModel();
        model.vendorExtensions = new HashMap<>();

        resolver.apply(model);

        assertFalse(model.vendorExtensions.containsKey("x-extra-imports"));
    }

    @Test
    @DisplayName("apply -> injects import when wrapper and mapping exists (x-data-item)")
    void apply_shouldInjectImport_fromDataItem() {
        ExternalModelRegistry registry = new ExternalModelRegistry();
        registry.register(
                Map.of(
                        "openapiGenerics.responseContract.CustomerDto",
                        "io.example.CustomerDto"));

        ExternalImportResolver resolver = new ExternalImportResolver(registry);

        CodegenModel model = new CodegenModel();
        model.name = "ServiceResponseCustomerDto";
        model.vendorExtensions = new HashMap<>();

        model.vendorExtensions.put("x-api-wrapper", true);
        model.vendorExtensions.put("x-data-item", "CustomerDto");

        resolver.apply(model);

        assertEquals(
                "io.example.CustomerDto",
                model.vendorExtensions.get("x-extra-imports"));
    }

    @Test
    @DisplayName("apply -> injects import when wrapper and mapping exists (x-api-wrapper-datatype)")
    void apply_shouldInjectImport_fromWrapperDatatype() {
        ExternalModelRegistry registry = new ExternalModelRegistry();
        registry.register(
                Map.of(
                        "openapiGenerics.responseContract.CustomerDto",
                        "io.example.CustomerDto"));

        ExternalImportResolver resolver = new ExternalImportResolver(registry);

        CodegenModel model = new CodegenModel();
        model.name = "ServiceResponseCustomerDto";
        model.vendorExtensions = new HashMap<>();

        model.vendorExtensions.put("x-api-wrapper", true);
        model.vendorExtensions.put("x-api-wrapper-datatype", "CustomerDto");

        resolver.apply(model);

        assertEquals(
                "io.example.CustomerDto",
                model.vendorExtensions.get("x-extra-imports"));
    }

    @Test
    @DisplayName("apply -> does nothing when mapping does not exist")
    void apply_shouldDoNothing_whenMappingMissing() {
        ExternalModelRegistry registry = new ExternalModelRegistry();
        ExternalImportResolver resolver = new ExternalImportResolver(registry);

        CodegenModel model = new CodegenModel();
        model.vendorExtensions = new HashMap<>();

        model.vendorExtensions.put("x-api-wrapper", true);
        model.vendorExtensions.put("x-data-item", "CustomerDto");

        resolver.apply(model);

        assertFalse(model.vendorExtensions.containsKey("x-extra-imports"));
    }

    @Test
    @DisplayName("apply -> does nothing when no type info present")
    void apply_shouldDoNothing_whenNoTypeInfo() {
        ExternalModelRegistry registry = new ExternalModelRegistry();
        ExternalImportResolver resolver = new ExternalImportResolver(registry);

        CodegenModel model = new CodegenModel();
        model.vendorExtensions = new HashMap<>();

        model.vendorExtensions.put("x-api-wrapper", true);

        resolver.apply(model);

        assertFalse(model.vendorExtensions.containsKey("x-extra-imports"));
    }

    @Test
    @DisplayName("apply -> safe when vendorExtensions is null")
    void apply_shouldBeSafe_whenVendorExtensionsNull() {
        ExternalModelRegistry registry = new ExternalModelRegistry();
        ExternalImportResolver resolver = new ExternalImportResolver(registry);

        CodegenModel model = new CodegenModel();
        model.vendorExtensions = null;

        assertDoesNotThrow(() -> resolver.apply(model));
    }
}