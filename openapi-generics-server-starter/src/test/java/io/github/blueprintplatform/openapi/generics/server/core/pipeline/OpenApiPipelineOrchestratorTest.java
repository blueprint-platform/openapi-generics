package io.github.blueprintplatform.openapi.generics.server.core.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeIntrospector;
import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaProcessor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.base.BaseSchemaRegistrar;
import io.github.blueprintplatform.openapi.generics.server.core.schema.base.SchemaGenerationControlMarker;
import io.github.blueprintplatform.openapi.generics.server.core.validation.OpenApiContractGuard;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;

class OpenApiPipelineOrchestratorTest {

  private final BaseSchemaRegistrar baseSchemaRegistrar = mock(BaseSchemaRegistrar.class);
  private final SchemaGenerationControlMarker marker = mock(SchemaGenerationControlMarker.class);
  private final ResponseTypeDiscoveryStrategy discovery = mock(ResponseTypeDiscoveryStrategy.class);
  private final ResponseTypeIntrospector introspector = mock(ResponseTypeIntrospector.class);
  private final WrapperSchemaProcessor processor = mock(WrapperSchemaProcessor.class);
  private final OpenApiContractGuard guard = mock(OpenApiContractGuard.class);

  private final OpenApiPipelineOrchestrator orchestrator =
      new OpenApiPipelineOrchestrator(
          baseSchemaRegistrar, marker, discovery, introspector, processor, guard);

  @Test
  @DisplayName("run -> should execute full pipeline")
  void shouldRunFullPipeline() {
    OpenAPI openApi = new OpenAPI().components(new Components());

    ResolvableType type =
        ResolvableType.forClassWithGenerics(
            io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse.class,
            CustomerDto.class);

    when(discovery.discover()).thenReturn(Set.of(type));
    when(introspector.extractDataRefName(type)).thenReturn(java.util.Optional.of("CustomerDto"));

    orchestrator.run(openApi);

    verify(baseSchemaRegistrar).register(openApi);
    verify(discovery).discover();
    verify(introspector).extractDataRefName(type);
    verify(processor).process(openApi, "CustomerDto");
    verify(marker).mark(openApi);
    verify(guard).validate(openApi);
  }

  @Test
  @DisplayName("run -> should skip duplicate execution")
  void shouldSkipDuplicateExecution() {
    OpenAPI openApi = new OpenAPI().components(new Components());

    when(discovery.discover()).thenReturn(Set.of());
    when(introspector.extractDataRefName(any())).thenReturn(java.util.Optional.empty());

    orchestrator.run(openApi);
    orchestrator.run(openApi); // ikinci çağrı

    verify(baseSchemaRegistrar, times(1)).register(openApi);
    verify(guard, times(1)).validate(openApi);
  }

  @Test
  @DisplayName("run -> should handle empty discovery")
  void shouldHandleEmptyDiscovery() {
    OpenAPI openApi = new OpenAPI().components(new Components());

    when(discovery.discover()).thenReturn(Set.of());

    orchestrator.run(openApi);

    verify(processor, never()).process(any(), any());
    verify(marker).mark(openApi);
    verify(guard).validate(openApi);
  }

  static class CustomerDto {}
}
