package io.github.blueprintplatform.openapi.generics.server.core.schema.base;

import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Sort;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Set;

public class SchemaGenerationControlMarker {

  public void mark(OpenAPI openApi, Set<ResponseTypeDescriptor> descriptors) {
    if (openApi == null
        || openApi.getComponents() == null
        || openApi.getComponents().getSchemas() == null
        || descriptors == null
        || descriptors.isEmpty()) {
            return;
        }

    for (ResponseTypeDescriptor descriptor : descriptors) {
      markIgnore(openApi, descriptor.envelopeType().getSimpleName());

      if (isDefaultEnvelope(descriptor)) {
        markIgnore(openApi, Meta.class.getSimpleName());
        markIgnore(openApi, Sort.class.getSimpleName());
        markIgnore(openApi, ServiceResponse.class.getSimpleName() + "Void");
      }

      if (descriptor.isContainer()) {
        markIgnore(openApi, descriptor.dataRefName());
      }
    }
  }

  private boolean isDefaultEnvelope(ResponseTypeDescriptor descriptor) {
    return ServiceResponse.class.equals(descriptor.envelopeType());
    }

  private void markIgnore(OpenAPI openApi, String schemaName) {
    Schema<?> schema = openApi.getComponents().getSchemas().get(schemaName);
        if (schema != null) {
            schema.addExtension(VendorExtensions.IGNORE_MODEL, true);
        }
    }
}