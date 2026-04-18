package io.github.blueprintplatform.openapi.generics.server.core.schema;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;

public final class ServiceResponseSchemaFactory {

  private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

  private ServiceResponseSchemaFactory() {}

  public static Schema<?> createComposedWrapper(
      Class<?> envelopeType, String payloadPropertyName, String dataRefName) {

    ComposedSchema schema = new ComposedSchema();

    schema.setAllOf(
        List.of(
            new Schema<>().$ref(buildRef(envelopeType.getSimpleName())),
            new ObjectSchema()
                .addProperty(payloadPropertyName, new Schema<>().$ref(buildRef(dataRefName)))));

    return schema;
  }

  private static String buildRef(String schemaName) {
    return SCHEMA_REF_PREFIX + schemaName;
  }
}