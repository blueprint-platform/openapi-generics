package io.github.blueprintplatform.openapi.generics.codegen;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ModelIgnoreDecider")
class ModelIgnoreDeciderTest {

  @Test
  @DisplayName("shouldIgnore -> true when x-ignore-model=true")
  void shouldIgnore_byExtension() {
    ExternalModelRegistry registry = new ExternalModelRegistry();
    ModelIgnoreDecider decider = new ModelIgnoreDecider(registry);

    Schema<?> schema = new Schema<>();
    schema.setExtensions(Map.of("x-ignore-model", true));

    boolean result = decider.shouldIgnore("CustomerDto", schema);

    assertTrue(result);
  }

  @Test
  @DisplayName("shouldIgnore -> true when model is registered as external")
  void shouldIgnore_byExternalRegistry() {
    ExternalModelRegistry registry = new ExternalModelRegistry();
    registry.register(
        Map.of("openapiGenerics.responseContract.CustomerDto", "io.example.CustomerDto"));

    ModelIgnoreDecider decider = new ModelIgnoreDecider(registry);

    Schema<?> schema = new Schema<>();

    boolean result = decider.shouldIgnore("CustomerDto", schema);

    assertTrue(result);
  }

  @Test
  @DisplayName("shouldIgnore -> false when no rules match")
  void shouldIgnore_false_whenNoMatch() {
    ExternalModelRegistry registry = new ExternalModelRegistry();
    ModelIgnoreDecider decider = new ModelIgnoreDecider(registry);

    Schema<?> schema = new Schema<>();

    boolean result = decider.shouldIgnore("CustomerDto", schema);

    assertFalse(result);
  }

  @Test
  @DisplayName("markIgnored + isIgnored -> should track ignored models")
  void markIgnored_shouldTrackState() {
    ExternalModelRegistry registry = new ExternalModelRegistry();
    ModelIgnoreDecider decider = new ModelIgnoreDecider(registry);

    decider.markIgnored("CustomerDto");

    assertTrue(decider.isIgnored("CustomerDto"));
    assertFalse(decider.isIgnored("OtherDto"));
  }

  @Test
  @DisplayName("shouldIgnore -> extension takes precedence (both true anyway)")
  void shouldIgnore_extensionAndExternal() {
    ExternalModelRegistry registry = new ExternalModelRegistry();
    registry.register(
        Map.of("openapiGenerics.responseContract.CustomerDto", "io.example.CustomerDto"));

    ModelIgnoreDecider decider = new ModelIgnoreDecider(registry);

    Schema<?> schema = new Schema<>();
    schema.setExtensions(Map.of("x-ignore-model", true));

    boolean result = decider.shouldIgnore("CustomerDto", schema);

    assertTrue(result);
  }

  @Test
  @DisplayName("shouldIgnore -> handles null schema safely")
  void shouldIgnore_nullSchema() {
    ExternalModelRegistry registry = new ExternalModelRegistry();
    ModelIgnoreDecider decider = new ModelIgnoreDecider(registry);

    boolean result = decider.shouldIgnore("CustomerDto", null);

    assertFalse(result);
  }
}
