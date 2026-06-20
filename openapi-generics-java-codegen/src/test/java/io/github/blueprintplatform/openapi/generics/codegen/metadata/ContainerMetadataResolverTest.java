package io.github.blueprintplatform.openapi.generics.codegen.metadata;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenProperties;
import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;

@Tag("unit")
@DisplayName("Unit Test: ContainerMetadataResolver")
class ContainerMetadataResolverTest {

    @Test
    @DisplayName("apply -> does nothing when model is not a wrapper")
    void apply_shouldDoNothing_whenNotWrapper() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        CodegenModel model = model();
        model.vendorExtensions.put(CodegenVendorExtensions.DATA_CONTAINER, "Page");

        resolver.apply(model);

        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("apply -> applies default Page container metadata")
    void apply_shouldApplyDefaultPageMetadata() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        CodegenModel model = wrapperModel("ServiceResponsePageCustomerDto", "Page");

        resolver.apply(model);

        assertEquals(
                Page.class.getSimpleName(),
                model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertEquals(
                Page.class.getCanonicalName(),
                model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("apply -> applies default List container metadata")
    void apply_shouldApplyDefaultListMetadata() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        CodegenModel model = wrapperModel("ServiceResponseListCustomerDto", "List");

        resolver.apply(model);

        assertEquals(
                List.class.getSimpleName(),
                model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertEquals(
                List.class.getCanonicalName(),
                model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("apply -> uses container name as type when mapping is missing")
    void apply_shouldUseContainerNameAsType_whenMappingMissing() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

        resolver.apply(model);

        assertEquals("Slice", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("apply -> does nothing when data container is missing")
    void apply_shouldDoNothing_whenDataContainerMissing() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        CodegenModel model = model();
        model.name = "ServiceResponseCustomerDto";
        model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER, true);

        resolver.apply(model);

        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("apply -> does nothing when data container is blank")
    void apply_shouldDoNothing_whenDataContainerBlank() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        CodegenModel model = wrapperModel("ServiceResponseCustomerDto", "   ");

        resolver.apply(model);

        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("apply -> safe when vendorExtensions is null")
    void apply_shouldBeSafe_whenVendorExtensionsNull() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        CodegenModel model = new CodegenModel();
        model.name = "ServiceResponsePageCustomerDto";
        model.vendorExtensions = null;

        assertDoesNotThrow(() -> resolver.apply(model));
    }

    @Test
    @DisplayName("register -> adds custom external container mapping")
    void register_shouldAddCustomExternalContainerMapping() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        resolver.register(
                Map.of(
                        CodegenProperties.DATA_CONTAINER_PREFIX + "Slice",
                        "io.example.contract.Slice"));

        CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

        resolver.apply(model);

        assertEquals("Slice", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertEquals(
                "io.example.contract.Slice",
                model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("register -> overrides default container mapping")
    void register_shouldOverrideDefaultContainerMapping() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        resolver.register(
                Map.of(
                        CodegenProperties.DATA_CONTAINER_PREFIX + "Page",
                        "io.example.contract.CustomPage"));

        CodegenModel model = wrapperModel("ServiceResponsePageCustomerDto", "Page");

        resolver.apply(model);

        assertEquals(
                "CustomPage", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertEquals(
                "io.example.contract.CustomPage",
                model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("register -> ignores unrelated properties")
    void register_shouldIgnoreUnrelatedProperties() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        resolver.register(Map.of("some.other.key", "io.example.contract.Slice"));

        CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

        resolver.apply(model);

        assertEquals("Slice", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("register -> ignores null properties")
    void register_shouldIgnoreNullProperties() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        assertDoesNotThrow(() -> resolver.register(null));
    }

    @Test
    @DisplayName("register -> ignores empty properties")
    void register_shouldIgnoreEmptyProperties() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        assertDoesNotThrow(() -> resolver.register(Map.of()));
    }

    @Test
    @DisplayName("register -> ignores null key")
    void register_shouldIgnoreNullKey() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        Map<String, Object> properties = new HashMap<>();
        properties.put(null, "io.example.contract.Slice");

        resolver.register(properties);

        CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

        resolver.apply(model);

        assertEquals("Slice", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("register -> ignores null value")
    void register_shouldIgnoreNullValue() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        Map<String, Object> properties = new HashMap<>();
        properties.put(CodegenProperties.DATA_CONTAINER_PREFIX + "Slice", null);

        resolver.register(properties);

        CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

        resolver.apply(model);

        assertEquals("Slice", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("register -> trims configured FQCN")
    void register_shouldTrimConfiguredFqcn() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        resolver.register(
                Map.of(
                        CodegenProperties.DATA_CONTAINER_PREFIX + "Slice",
                        "  io.example.contract.Slice  "));

        CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

        resolver.apply(model);

        assertEquals("Slice", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertEquals(
                "io.example.contract.Slice",
                model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("register -> ignores blank FQCN")
    void register_shouldIgnoreBlankFqcn() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        resolver.register(Map.of(CodegenProperties.DATA_CONTAINER_PREFIX + "Slice", "   "));

        CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

        resolver.apply(model);

        assertEquals("Slice", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("register -> ignores literal null FQCN")
    void register_shouldIgnoreLiteralNullFqcn() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        resolver.register(Map.of(CodegenProperties.DATA_CONTAINER_PREFIX + "Slice", "null"));

        CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

        resolver.apply(model);

        assertEquals("Slice", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("register -> ignores non-qualified class name")
    void register_shouldIgnoreNonQualifiedClassName() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        resolver.register(Map.of(CodegenProperties.DATA_CONTAINER_PREFIX + "Slice", "Slice"));

        CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

        resolver.apply(model);

        assertEquals("Slice", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    @Test
    @DisplayName("register -> converts non-string FQCN values")
    void register_shouldConvertNonStringFqcnValues() {
        ContainerMetadataResolver resolver = new ContainerMetadataResolver();

        resolver.register(Map.of(CodegenProperties.DATA_CONTAINER_PREFIX + "Slice", new FqcnValue()));

        CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

        resolver.apply(model);

        assertEquals("Slice", model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_TYPE));
        assertEquals(
                "io.example.contract.Slice",
                model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
    }

    private CodegenModel wrapperModel(String name, String container) {
        CodegenModel model = model();
        model.name = name;
        model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER, true);
        model.vendorExtensions.put(CodegenVendorExtensions.DATA_CONTAINER, container);
        return model;
    }

    private CodegenModel model() {
        CodegenModel model = new CodegenModel();
        model.vendorExtensions = new HashMap<>();
        return model;
    }

    private static final class FqcnValue {

        @Override
        public String toString() {
            return "io.example.contract.Slice";
        }
    }
}