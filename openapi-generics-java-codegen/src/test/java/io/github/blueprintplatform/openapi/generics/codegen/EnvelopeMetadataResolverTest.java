package io.github.blueprintplatform.openapi.generics.codegen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;

@Tag("unit")
@DisplayName("Unit Test: EnvelopeMetadataResolver")
class EnvelopeMetadataResolverTest {

  @Test
  @DisplayName("register -> should use default envelope when property is missing")
  void register_shouldUseDefault_whenNotConfigured() {
    EnvelopeMetadataResolver resolver = new EnvelopeMetadataResolver();

    resolver.register(Map.of());

    CodegenModel model = wrapperModel();
    resolver.apply(model);

    Map<String, Object> ve = model.getVendorExtensions();

    assertEquals(
        "io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse",
        ve.get("x-envelope-import"));
    assertEquals("ServiceResponse", ve.get("x-envelope-type"));
  }

  @Test
  @DisplayName("register -> should override envelope when configured")
  void register_shouldOverrideEnvelope_whenConfigured() {
    EnvelopeMetadataResolver resolver = new EnvelopeMetadataResolver();

    resolver.register(Map.of("openapi-generics.envelope", "com.example.ApiResponse"));

    CodegenModel model = wrapperModel();
    resolver.apply(model);

    Map<String, Object> ve = model.getVendorExtensions();

    assertEquals("com.example.ApiResponse", ve.get("x-envelope-import"));
    assertEquals("ApiResponse", ve.get("x-envelope-type"));
  }

  @Test
  @DisplayName("apply -> should not modify non-wrapper models")
  void apply_shouldSkip_whenNotWrapper() {
    EnvelopeMetadataResolver resolver = new EnvelopeMetadataResolver();

    resolver.register(Map.of("openapi-generics.envelope", "com.example.ApiResponse"));

    CodegenModel model = new CodegenModel();
    model.vendorExtensions = new HashMap<>();

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey("x-envelope-import"));
    assertFalse(model.vendorExtensions.containsKey("x-envelope-type"));
  }

  @Test
  @DisplayName("apply -> should handle null vendorExtensions safely")
  void apply_shouldSkip_whenVendorExtensionsNull() {
    EnvelopeMetadataResolver resolver = new EnvelopeMetadataResolver();

    resolver.register(Map.of("openapi-generics.envelope", "com.example.ApiResponse"));

    CodegenModel model = wrapperModel();
    model.vendorExtensions = null;

    resolver.apply(model);

    assertNull(model.vendorExtensions);
  }

  @Test
  @DisplayName("register -> should ignore blank configuration")
  void register_shouldIgnoreBlankValue() {
    EnvelopeMetadataResolver resolver = new EnvelopeMetadataResolver();

    resolver.register(Map.of("openapi-generics.envelope", "   "));

    CodegenModel model = wrapperModel();
    resolver.apply(model);

    Map<String, Object> ve = model.getVendorExtensions();

    assertEquals(
        "io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse",
        ve.get("x-envelope-import"));
    assertEquals("ServiceResponse", ve.get("x-envelope-type"));
  }

  private CodegenModel wrapperModel() {
    CodegenModel model = new CodegenModel();
    model.vendorExtensions = new HashMap<>();
    model.vendorExtensions.put("x-api-wrapper", true);
    return model;
  }
}
