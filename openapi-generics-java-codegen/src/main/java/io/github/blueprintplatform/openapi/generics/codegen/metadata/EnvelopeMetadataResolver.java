package io.github.blueprintplatform.openapi.generics.codegen.metadata;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenProperties;
import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import java.util.Map;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvelopeMetadataResolver {

  private static final Logger log = LoggerFactory.getLogger(EnvelopeMetadataResolver.class);

  private String envelopeImport = ServiceResponse.class.getCanonicalName();
  private String envelopeType = ServiceResponse.class.getSimpleName();

  public void register(Map<String, Object> additionalProperties) {
    Object configured = additionalProperties.get(CodegenProperties.ENVELOPE);

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

    Map<String, Object> vendorExtensions = model.getVendorExtensions();

    if (vendorExtensions == null) {
      return;
    }

    vendorExtensions.put(CodegenVendorExtensions.ENVELOPE_IMPORT, envelopeImport);
    vendorExtensions.put(CodegenVendorExtensions.ENVELOPE_TYPE, envelopeType);

    log.debug("Envelope metadata applied to wrapper model: {} -> {}", model.name, envelopeType);
  }

  private boolean isWrapperModel(CodegenModel model) {
    Map<String, Object> vendorExtensions = model.getVendorExtensions();

    return vendorExtensions != null
            && Boolean.TRUE.equals(vendorExtensions.get(CodegenVendorExtensions.API_WRAPPER));
  }

  private String extractSimpleName(String fqcn) {
    int lastDot = fqcn.lastIndexOf('.');

    return lastDot >= 0 ? fqcn.substring(lastDot + 1) : fqcn;
  }
}