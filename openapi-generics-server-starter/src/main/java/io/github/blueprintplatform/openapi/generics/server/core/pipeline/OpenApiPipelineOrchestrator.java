package io.github.blueprintplatform.openapi.generics.server.core.pipeline;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeIntrospector;
import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaProcessor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.control.SchemaGenerationControlMarker;
import io.github.blueprintplatform.openapi.generics.server.core.validation.OpenApiContractGuard;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrates the full OpenAPI projection pipeline for contract-aware responses.
 *
 * <p>This is the single entry point that coordinates all processing steps:
 *
 * <ol>
 *   <li>Discover response types from the application layer
 *   <li>Extract contract-aware descriptors via introspection
 *   <li>Generate wrapper schemas (default or BYOE)
 *   <li>Mark non-authoritative schemas to be ignored
 *   <li>Validate final OpenAPI contract integrity
 * </ol>
 *
 * <p>The pipeline is executed exactly once per OpenAPI instance.
 *
 * <p><b>Design principles:</b>
 *
 * <ul>
 *   <li>Deterministic execution order
 *   <li>Contract-first enforcement (no drift allowed)
 *   <li>OpenAPI is treated as a projection, not a source of truth
 * </ul>
 *
 * <p>Acts as the integration point between discovery, schema generation,
 * control marking, and contract validation phases.
 */
public class OpenApiPipelineOrchestrator {

  private static final Logger log = LoggerFactory.getLogger(OpenApiPipelineOrchestrator.class);

  private final Set<OpenAPI> processed = Collections.newSetFromMap(new IdentityHashMap<>());

  private final SchemaGenerationControlMarker schemaGenerationControlMarker;
  private final ResponseTypeDiscoveryStrategy discoveryStrategy;
  private final ResponseTypeIntrospector introspector;
  private final WrapperSchemaProcessor wrapperSchemaProcessor;
  private final OpenApiContractGuard contractGuard;

  public OpenApiPipelineOrchestrator(
      SchemaGenerationControlMarker schemaGenerationControlMarker,
      ResponseTypeDiscoveryStrategy discoveryStrategy,
      ResponseTypeIntrospector introspector,
      WrapperSchemaProcessor wrapperSchemaProcessor,
      OpenApiContractGuard contractGuard) {

    this.schemaGenerationControlMarker = schemaGenerationControlMarker;
    this.discoveryStrategy = discoveryStrategy;
    this.introspector = introspector;
    this.wrapperSchemaProcessor = wrapperSchemaProcessor;
    this.contractGuard = contractGuard;
  }

  public void run(OpenAPI openApi) {
    if (!processed.add(openApi)) {
      log.debug("Pipeline already executed → skipping");
      return;
    }

    log.debug("OpenAPI pipeline started");

    Set<ResponseTypeDescriptor> descriptors = discoverDescriptors();
    log.debug("Discovered {} contract-aware response types", descriptors.size());

    descriptors.forEach(descriptor -> wrapperSchemaProcessor.process(openApi, descriptor));
    log.debug("Processed {} wrapper schemas", descriptors.size());

    schemaGenerationControlMarker.mark(openApi, descriptors);
    log.debug("Applied ignore markers to managed schemas");

    contractGuard.validate(openApi, descriptors);

    log.debug("OpenAPI pipeline completed successfully");
  }

  private Set<ResponseTypeDescriptor> discoverDescriptors() {
    Set<ResponseTypeDescriptor> discovered = new LinkedHashSet<>();

    discoveryStrategy
        .discover()
        .forEach(type -> introspector.extract(type).ifPresent(discovered::add));

    return discovered;
  }
}