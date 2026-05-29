package io.github.blueprintplatform.openapi.generics.server.core.schema;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

/**
 * Applies canonical wrapper metadata to generated OpenAPI wrapper schemas.
 *
 * <p>Enriches projected response wrapper schemas with the vendor extensions required by the
 * OpenAPI Generics code generation contract.
 */
public final class ServiceResponseSchemaFactory {

  private ServiceResponseSchemaFactory() {}

  public static Schema<?> enrichComposedWrapper(
      Map<String, Schema> schemas, ResponseTypeDescriptor descriptor) {

    String wrapperName = descriptor.envelopeType().getSimpleName() + descriptor.dataRefName();
    Schema<?> wrapper = schemas.get(wrapperName);

    if (wrapper == null) {
      throw new IllegalStateException("Missing wrapper schema: " + wrapperName);
    }

    wrapper.addExtension(VendorExtensions.API_WRAPPER, Boolean.TRUE);
    wrapper.addExtension(VendorExtensions.API_WRAPPER_DATATYPE, descriptor.dataRefName());

    return wrapper;
  }
}
