package io.github.blueprintplatform.openapi.generics.server.core.schema;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes wrapper schemas during OpenAPI projection.
 *
 * <p>Builds the composed wrapper schema for the given response descriptor (applies to both default
 * and custom envelopes).
 *
 * <p>If the envelope is the default ServiceResponse, additional schema enrichment is applied.
 * Custom envelopes (BYOE) are not further enriched.
 */
public class WrapperSchemaProcessor {

    private static final Logger log = LoggerFactory.getLogger(WrapperSchemaProcessor.class);

    private final WrapperSchemaEnricher enricher;

    public WrapperSchemaProcessor(WrapperSchemaEnricher enricher) {
        this.enricher = enricher;
    }

  public void process(OpenAPI openApi, ResponseTypeDescriptor descriptor) {
        Map<String, Schema> schemas = openApi.getComponents().getSchemas();

    Schema<?> wrapper =
        ServiceResponseSchemaFactory.enrichComposedWrapper(schemas, descriptor);

    log.debug("Wrapper schema '{}' enriched", wrapper.getName());

    if (isDefaultEnvelope(descriptor)) {
        enricher.enrich(openApi, wrapper.getName(), descriptor.dataRefName());
    }
  }

  private boolean isDefaultEnvelope(ResponseTypeDescriptor descriptor) {
    return ServiceResponse.class.equals(descriptor.envelopeType());
    }

}