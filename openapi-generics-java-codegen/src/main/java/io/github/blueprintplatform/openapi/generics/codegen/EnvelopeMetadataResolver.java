package io.github.blueprintplatform.openapi.generics.codegen;

import java.util.Map;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves and injects envelope type metadata into wrapper models during code generation.
 *
 * <p>Supports BYOE (Bring Your Own Envelope) by allowing a custom envelope type to be configured
 * via generator properties.
 *
 * <p>Adds required vendor extensions so templates can reference the correct envelope type without
 * generating it.
 */
public class EnvelopeMetadataResolver {

  private static final Logger log = LoggerFactory.getLogger(EnvelopeMetadataResolver.class);

  private static final String PROPERTY_ENVELOPE = "openapi-generics.envelope";

  private static final String DEFAULT_ENVELOPE_IMPORT =
      "io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse";
  private static final String DEFAULT_ENVELOPE_TYPE = "ServiceResponse";

  private static final String EXT_API_WRAPPER = "x-api-wrapper";
  private static final String EXT_ENVELOPE_IMPORT = "x-envelope-import";
  private static final String EXT_ENVELOPE_TYPE = "x-envelope-type";

  private String envelopeImport = DEFAULT_ENVELOPE_IMPORT;
  private String envelopeType = DEFAULT_ENVELOPE_TYPE;

  public void register(Map<String, Object> additionalProperties) {
    Object configured = additionalProperties.get(PROPERTY_ENVELOPE);
    if (!(configured instanceof String fqcn) || fqcn.isBlank()) {
      return;
    }

    envelopeImport = fqcn.trim();
    envelopeType = extractSimpleName(envelopeImport);

    log.debug("Configured envelope metadata: {} -> {}", envelopeImport, envelopeType);
  }

  public void apply(CodegenModel model) {
    if (!isWrapperModel(model)) {
      return;
    }

    Map<String, Object> ve = model.getVendorExtensions();
    if (ve == null) {
      return;
    }

    ve.put(EXT_ENVELOPE_IMPORT, envelopeImport);
    ve.put(EXT_ENVELOPE_TYPE, envelopeType);

    log.debug("Envelope metadata applied to wrapper model: {} -> {}", model.name, envelopeType);
  }

  private boolean isWrapperModel(CodegenModel model) {
    Map<String, Object> ve = model.getVendorExtensions();
    return ve != null && Boolean.TRUE.equals(ve.get(EXT_API_WRAPPER));
  }

  private String extractSimpleName(String fqcn) {
    int lastDot = fqcn.lastIndexOf('.');
    return lastDot >= 0 ? fqcn.substring(lastDot + 1) : fqcn;
  }
}
