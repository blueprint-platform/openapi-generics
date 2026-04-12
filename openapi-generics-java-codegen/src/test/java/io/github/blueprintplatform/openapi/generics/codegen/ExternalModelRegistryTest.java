package io.github.blueprintplatform.openapi.generics.codegen;

import static org.junit.jupiter.api.Assertions.*;

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
            "openapiGenerics.responseContract.CustomerDto",
            "io.example.CustomerDto",
            "openapiGenerics.responseContract.OrderDto",
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
            "some.other.key", "value",
            "openapiGenerics.wrongPrefix.CustomerDto", "io.example.CustomerDto");

    registry.register(props);

    assertFalse(registry.isExternal("CustomerDto"));
    assertNull(registry.getFqcn("CustomerDto"));
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
        Map.of("openapiGenerics.responseContract.CustomerDto", "io.example.CustomerDtoV1"));

    registry.register(
        Map.of("openapiGenerics.responseContract.CustomerDto", "io.example.CustomerDtoV2"));

    assertEquals("io.example.CustomerDtoV2", registry.getFqcn("CustomerDto"));
  }
}
