package io.github.blueprintplatform.openapi.generics.codegen.metadata;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenProperties;
import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.codegen.CodegenModel;

@Tag("unit")
@DisplayName("Unit Test: ContainerImportResolver")
class ContainerImportResolverTest {

  @Test
  @DisplayName("apply -> does nothing when model is not a wrapper")
  void apply_shouldDoNothing_whenNotWrapper() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    CodegenModel model = model();
    model.vendorExtensions.put(CodegenVendorExtensions.DATA_CONTAINER, "Page");

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("apply -> applies default Page container import")
  void apply_shouldApplyDefaultPageImport() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    CodegenModel model = wrapperModel("ServiceResponsePageCustomerDto", "Page");

    resolver.apply(model);

    assertEquals(
            Page.class.getCanonicalName(),
            model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @ParameterizedTest
  @ValueSource(strings = {"List", "Set"})
  @DisplayName("apply -> does not add imports for Java collection containers")
  void apply_shouldNotAddImport_forJavaCollectionContainers(String container) {
    ContainerImportResolver resolver = new ContainerImportResolver();

    CodegenModel model = wrapperModel("ServiceResponse" + container + "CustomerDto", container);

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("apply -> does nothing when container mapping is missing")
  void apply_shouldDoNothing_whenMappingMissing() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("apply -> does nothing when data container is missing")
  void apply_shouldDoNothing_whenDataContainerMissing() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    CodegenModel model = model();
    model.name = "ServiceResponseCustomerDto";
    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER, true);

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("apply -> does nothing when data container is blank")
  void apply_shouldDoNothing_whenDataContainerBlank() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    CodegenModel model = wrapperModel("ServiceResponseCustomerDto", "   ");

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("apply -> safe when vendorExtensions is null")
  void apply_shouldBeSafe_whenVendorExtensionsNull() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    CodegenModel model = new CodegenModel();
    model.name = "ServiceResponsePageCustomerDto";
    model.vendorExtensions = null;

    assertDoesNotThrow(() -> resolver.apply(model));
  }

  @Test
  @DisplayName("register -> adds custom external container import")
  void register_shouldAddCustomExternalContainerImport() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    resolver.register(
            Map.of(CodegenProperties.DATA_CONTAINER_PREFIX + "Slice", "io.example.contract.Slice"));

    CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

    resolver.apply(model);

    assertEquals(
            "io.example.contract.Slice",
            model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("register -> overrides default Page container import")
  void register_shouldOverrideDefaultPageContainerImport() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    resolver.register(
            Map.of(CodegenProperties.DATA_CONTAINER_PREFIX + "Page", "io.example.contract.CustomPage"));

    CodegenModel model = wrapperModel("ServiceResponsePageCustomerDto", "Page");

    resolver.apply(model);

    assertEquals(
            "io.example.contract.CustomPage",
            model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("register -> ignores unrelated properties")
  void register_shouldIgnoreUnrelatedProperties() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    resolver.register(Map.of("some.other.key", "io.example.contract.Slice"));

    CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("register -> ignores null properties")
  void register_shouldIgnoreNullProperties() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    assertDoesNotThrow(() -> resolver.register(null));
  }

  @Test
  @DisplayName("register -> ignores empty properties")
  void register_shouldIgnoreEmptyProperties() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    assertDoesNotThrow(() -> resolver.register(Map.of()));
  }

  @Test
  @DisplayName("register -> ignores null key")
  void register_shouldIgnoreNullKey() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    Map<String, Object> properties = new HashMap<>();
    properties.put(null, "io.example.contract.Slice");

    resolver.register(properties);

    CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("register -> ignores null value")
  void register_shouldIgnoreNullValue() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    Map<String, Object> properties = new HashMap<>();
    properties.put(CodegenProperties.DATA_CONTAINER_PREFIX + "Slice", null);

    resolver.register(properties);

    CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("register -> trims configured FQCN")
  void register_shouldTrimConfiguredFqcn() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    resolver.register(
            Map.of(CodegenProperties.DATA_CONTAINER_PREFIX + "Slice", "  io.example.contract.Slice  "));

    CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

    resolver.apply(model);

    assertEquals(
            "io.example.contract.Slice",
            model.vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @ParameterizedTest
  @ValueSource(strings = {"   ", "null", "Slice"})
  @DisplayName("register -> ignores invalid FQCN")
  void register_shouldIgnoreInvalidFqcn(String fqcn) {
    ContainerImportResolver resolver = new ContainerImportResolver();

    resolver.register(Map.of(CodegenProperties.DATA_CONTAINER_PREFIX + "Slice", fqcn));

    CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.DATA_CONTAINER_IMPORT));
  }

  @Test
  @DisplayName("register -> converts non-string FQCN values")
  void register_shouldConvertNonStringFqcnValues() {
    ContainerImportResolver resolver = new ContainerImportResolver();

    resolver.register(Map.of(CodegenProperties.DATA_CONTAINER_PREFIX + "Slice", new FqcnValue()));

    CodegenModel model = wrapperModel("ServiceResponseSliceCustomerDto", "Slice");

    resolver.apply(model);

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