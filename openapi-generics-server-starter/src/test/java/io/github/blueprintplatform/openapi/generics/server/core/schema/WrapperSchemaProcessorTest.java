package io.github.blueprintplatform.openapi.generics.server.core.schema;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: WrapperSchemaProcessor")
class WrapperSchemaProcessorTest {

  @Test
  @DisplayName(
      "process -> should enrich wrapper and delegate container enrichment for default envelope")
  void process_shouldDelegateEnricher_whenDefaultEnvelope() {
    WrapperSchemaEnricher enricher = mock(WrapperSchemaEnricher.class);
    WrapperSchemaProcessor processor = new WrapperSchemaProcessor(enricher);

    OpenAPI openApi = openApiWithWrapper("ServiceResponseCustomerDto");
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    processor.process(openApi, descriptor);

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponseCustomerDto");

    assertNotNull(wrapper);
    assertEquals(Boolean.TRUE, wrapper.getExtensions().get("x-api-wrapper"));
    assertEquals("CustomerDto", wrapper.getExtensions().get("x-api-wrapper-datatype"));

    verify(enricher).enrich(openApi, "ServiceResponseCustomerDto", "CustomerDto");
    verifyNoMoreInteractions(enricher);
  }

  @Test
  @DisplayName("process -> should enrich wrapper but skip container enrichment for custom envelope")
  void process_shouldSkipEnricher_whenCustomEnvelope() {
    WrapperSchemaEnricher enricher = mock(WrapperSchemaEnricher.class);
    WrapperSchemaProcessor processor = new WrapperSchemaProcessor(enricher);

    OpenAPI openApi = openApiWithWrapper("ApiResponseCustomerDto");
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ApiResponse.class, "data", "CustomerDto");

    processor.process(openApi, descriptor);

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ApiResponseCustomerDto");

    assertNotNull(wrapper);
    assertEquals(Boolean.TRUE, wrapper.getExtensions().get("x-api-wrapper"));
    assertEquals("CustomerDto", wrapper.getExtensions().get("x-api-wrapper-datatype"));

    verifyNoInteractions(enricher);
  }

  @Test
  @DisplayName(
      "process -> should delegate container enrichment for default envelope container response")
  void process_shouldDelegateEnricher_forDefaultEnvelopeContainerResponse() {
    WrapperSchemaEnricher enricher = mock(WrapperSchemaEnricher.class);
    WrapperSchemaProcessor processor = new WrapperSchemaProcessor(enricher);

    OpenAPI openApi = openApiWithWrapper("ServiceResponsePageCustomerDto");
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.container(ServiceResponse.class, "data", "Page", "CustomerDto");

    processor.process(openApi, descriptor);

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertNotNull(wrapper);
    assertEquals(Boolean.TRUE, wrapper.getExtensions().get("x-api-wrapper"));
    assertEquals("PageCustomerDto", wrapper.getExtensions().get("x-api-wrapper-datatype"));

    verify(enricher).enrich(openApi, "ServiceResponsePageCustomerDto", "PageCustomerDto");
    verifyNoMoreInteractions(enricher);
  }

  @Test
  @DisplayName("process -> should fail when wrapper schema is missing")
  void process_shouldFail_whenWrapperMissing() {
    WrapperSchemaEnricher enricher = mock(WrapperSchemaEnricher.class);
    WrapperSchemaProcessor processor = new WrapperSchemaProcessor(enricher);

    OpenAPI openApi = new OpenAPI().components(new Components().schemas(new LinkedHashMap<>()));
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> processor.process(openApi, descriptor));

    assertTrue(ex.getMessage().contains("Missing wrapper schema"));
    verifyNoInteractions(enricher);
  }

  private OpenAPI openApiWithWrapper(String wrapperName) {
    Map<String, Schema> schemas = new LinkedHashMap<>();
    Schema<?> wrapper = new Schema<>().name(wrapperName);
    schemas.put(wrapperName, wrapper);

    return new OpenAPI().components(new Components().schemas(schemas));
  }

  static final class ApiResponse<T> {
    T data;
  }
}
