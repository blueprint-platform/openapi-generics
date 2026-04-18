package io.github.blueprintplatform.openapi.generics.server.core.schema;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.SchemaNames;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WrapperSchemaProcessorTest {

  private final WrapperSchemaEnricher enricher = mock(WrapperSchemaEnricher.class);

  private final WrapperSchemaProcessor processor =
      new WrapperSchemaProcessor(enricher);

  @Test
  @DisplayName("process -> should create wrapper schema")
  void shouldCreateWrapperSchema() {
    OpenAPI openApi = new OpenAPI().components(new Components().schemas(new HashMap<>()));

    processor.process(openApi, "CustomerDto");

    Schema<?> schema =
        openApi.getComponents().getSchemas().get(SchemaNames.SERVICE_RESPONSE + "CustomerDto");

    assertNotNull(schema);
    assertNotNull(schema.getAllOf());
    assertFalse(schema.getAllOf().isEmpty());

    verify(enricher).enrich(openApi, SchemaNames.SERVICE_RESPONSE + "CustomerDto", "CustomerDto");
  }

  @Test
  @DisplayName("process -> should overwrite existing schema")
  void shouldOverwriteExistingSchema() {
    OpenAPI openApi = new OpenAPI().components(new Components().schemas(new HashMap<>()));

    String wrapperName = SchemaNames.SERVICE_RESPONSE + "CustomerDto";

    Schema<?> oldSchema = new Schema<>();
    openApi.getComponents().getSchemas().put(wrapperName, oldSchema);

    processor.process(openApi, "CustomerDto");

    Schema<?> newSchema = openApi.getComponents().getSchemas().get(wrapperName);

    assertNotSame(oldSchema, newSchema);

    verify(enricher).enrich(openApi, wrapperName, "CustomerDto");
  }


  @Test
  @DisplayName("process -> should not fail with empty schemas map")
  void shouldHandleEmptySchemas() {
    OpenAPI openApi = new OpenAPI().components(new Components().schemas(new HashMap<>()));

    assertDoesNotThrow(() -> processor.process(openApi, "CustomerDto"));
  }
}
