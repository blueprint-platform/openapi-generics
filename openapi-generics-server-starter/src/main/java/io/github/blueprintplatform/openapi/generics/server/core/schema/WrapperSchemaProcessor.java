package io.github.blueprintplatform.openapi.generics.server.core.schema;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.enrichment.WrapperSchemaEnricher;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes wrapper schemas during OpenAPI projection.
 *
 * <p>Applies wrapper metadata for the given response descriptor and enriches default-envelope
 * container responses with container metadata.
 */
public class WrapperSchemaProcessor {

  private static final Logger log = LoggerFactory.getLogger(WrapperSchemaProcessor.class);

  private final WrapperSchemaEnricher enricher;

  public WrapperSchemaProcessor(WrapperSchemaEnricher enricher) {
    this.enricher = enricher;
  }

  @SuppressWarnings("rawtypes")
  public void process(OpenAPI openApi, ResponseTypeDescriptor descriptor) {
    Map<String, Schema> schemas = openApi.getComponents().getSchemas();

    Schema<?> wrapper = WrapperSchemaMetadataApplier.apply(schemas, descriptor);

    log.debug("Wrapper schema '{}' enriched", wrapper.getName());

    if (descriptor.isContainer()) {
      enricher.enrich(openApi, wrapper.getName(), descriptor);
    }
  }
}
