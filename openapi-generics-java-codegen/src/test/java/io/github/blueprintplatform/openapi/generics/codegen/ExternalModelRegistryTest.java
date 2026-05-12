package io.github.blueprintplatform.openapi.generics.codegen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ExternalModelRegistry")
class ExternalModelRegistryTest {

  @Test
  @DisplayName("register -> should store external model mappings")
  void register_shouldStoreMappings() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    Map<String, Object> props =
            Map.of(
                    "openapi-generics.response-contract.CustomerDto",
                    "io.example.CustomerDto",
                    "openapi-generics.response-contract.OrderDto",
                    "io.example.OrderDto");

    registry.register(props);

    assertTrue(registry.isExternal("CustomerDto"));
    assertTrue(registry.isExternal("OrderDto"));
    assertEquals("io.example.CustomerDto", registry.getFqcn("CustomerDto"));
    assertEquals("io.example.OrderDto", registry.getFqcn("OrderDto"));
  }

  @Test
  @DisplayName("register -> should ignore unrelated properties")
  void register_shouldIgnoreUnrelatedKeys() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    Map<String, Object> props =
            Map.of(
                    "some.other.key",
                    "value",
                    "openapiGenerics.wrongPrefix.CustomerDto",
                    "io.example.CustomerDto");

    registry.register(props);

    assertFalse(registry.isExternal("CustomerDto"));
    assertNull(registry.getFqcn("CustomerDto"));
  }

  @Test
  @DisplayName("register -> should ignore null properties")
  void register_shouldIgnoreNullProperties() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    registry.register(null);

    assertFalse(registry.isExternal("CustomerDto"));
    assertNull(registry.getFqcn("CustomerDto"));
  }

  @Test
  @DisplayName("register -> should ignore empty properties")
  void register_shouldIgnoreEmptyProperties() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    registry.register(Collections.emptyMap());

    assertFalse(registry.isExternal("CustomerDto"));
    assertNull(registry.getFqcn("CustomerDto"));
  }

  @Test
  @DisplayName("register -> should ignore null key")
  void register_shouldIgnoreNullKey() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    Map<String, Object> props = new HashMap<>();
    props.put(null, "io.example.CustomerDto");

    registry.register(props);

    assertFalse(registry.isExternal("CustomerDto"));
    assertNull(registry.getFqcn("CustomerDto"));
  }

  @Test
  @DisplayName("register -> should ignore null value")
  void register_shouldIgnoreNullValue() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    Map<String, Object> props = new HashMap<>();
    props.put("openapi-generics.response-contract.CustomerDto", null);

    registry.register(props);

    assertFalse(registry.isExternal("CustomerDto"));
    assertNull(registry.getFqcn("CustomerDto"));
  }

  @Test
  @DisplayName("register -> should trim configured FQCN")
  void register_shouldTrimConfiguredFqcn() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    registry.register(
            Map.of("openapi-generics.response-contract.CustomerDto", "  io.example.CustomerDto  "));

    assertTrue(registry.isExternal("CustomerDto"));
    assertEquals("io.example.CustomerDto", registry.getFqcn("CustomerDto"));
  }

  @Test
  @DisplayName("register -> should ignore blank FQCN")
  void register_shouldIgnoreBlankFqcn() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    registry.register(Map.of("openapi-generics.response-contract.CustomerDto", "   "));

    assertFalse(registry.isExternal("CustomerDto"));
    assertNull(registry.getFqcn("CustomerDto"));
  }

  @Test
  @DisplayName("register -> should ignore literal null FQCN")
  void register_shouldIgnoreLiteralNullFqcn() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    registry.register(Map.of("openapi-generics.response-contract.CustomerDto", "null"));

    assertFalse(registry.isExternal("CustomerDto"));
    assertNull(registry.getFqcn("CustomerDto"));
  }

  @Test
  @DisplayName("register -> should ignore non-qualified class name")
  void register_shouldIgnoreNonQualifiedClassName() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    registry.register(Map.of("openapi-generics.response-contract.CustomerDto", "CustomerDto"));

    assertFalse(registry.isExternal("CustomerDto"));
    assertNull(registry.getFqcn("CustomerDto"));
  }

  @Test
  @DisplayName("register -> should convert non-string FQCN values")
  void register_shouldConvertNonStringFqcnValues() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    registry.register(Map.of("openapi-generics.response-contract.CustomerDto", new FqcnValue()));

    assertTrue(registry.isExternal("CustomerDto"));
    assertEquals("io.example.CustomerDto", registry.getFqcn("CustomerDto"));
  }

  @Test
  @DisplayName("isExternal -> returns false for unknown model")
  void isExternal_shouldReturnFalse_whenUnknown() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    assertFalse(registry.isExternal("Unknown"));
  }

  @Test
  @DisplayName("getFqcn -> returns null for unknown model")
  void getFqcn_shouldReturnNull_whenUnknown() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    assertNull(registry.getFqcn("Unknown"));
  }

  @Test
  @DisplayName("register -> should override existing mapping")
  void register_shouldOverrideExistingMapping() {
    ExternalModelRegistry registry = new ExternalModelRegistry();

    registry.register(
            Map.of("openapi-generics.response-contract.CustomerDto", "io.example.CustomerDtoV1"));

    registry.register(
            Map.of("openapi-generics.response-contract.CustomerDto", "io.example.CustomerDtoV2"));

    assertEquals("io.example.CustomerDtoV2", registry.getFqcn("CustomerDto"));
  }

  private static final class FqcnValue {

    @Override
    public String toString() {
      return "io.example.CustomerDto";
    }
  }
}