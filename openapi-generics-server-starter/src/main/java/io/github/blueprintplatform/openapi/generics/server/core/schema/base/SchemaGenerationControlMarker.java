package io.github.blueprintplatform.openapi.generics.server.core.schema.base;

import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Sort;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SchemaGenerationControlMarker {

    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

    public void mark(OpenAPI openApi, Set<ResponseTypeDescriptor> descriptors) {
        if (openApi == null
                || openApi.getComponents() == null
                || openApi.getComponents().getSchemas() == null
                || descriptors == null
                || descriptors.isEmpty()) {
            return;
        }

        Map<String, Schema> schemas = openApi.getComponents().getSchemas();

        for (ResponseTypeDescriptor descriptor : descriptors) {
            markIgnore(schemas, descriptor.envelopeType().getSimpleName());

            if (isDefaultEnvelope(descriptor)) {
                markIgnore(schemas, Meta.class.getSimpleName());
                markIgnore(schemas, Sort.class.getSimpleName());
            }

            markNestedEnvelopeSchemas(schemas, descriptor);

            if (descriptor.isContainer()) {
                markIgnore(schemas, descriptor.dataRefName());
            }
        }
    }

    private void markNestedEnvelopeSchemas(
            Map<String, Schema> schemas,
            ResponseTypeDescriptor descriptor) {

        String wrapperName = descriptor.envelopeType().getSimpleName() + descriptor.dataRefName();
        Schema<?> wrapperSchema = schemas.get(wrapperName);
        if (wrapperSchema == null || wrapperSchema.getProperties() == null) {
            return;
        }

        Set<String> ignoreCandidates = new LinkedHashSet<>();

        for (Map.Entry<String, Schema> entry : wrapperSchema.getProperties().entrySet()) {
            String propertyName = entry.getKey();
            Schema<?> propertySchema = entry.getValue();

            if (descriptor.payloadPropertyName().equals(propertyName)) {
                continue;
            }

            collectReferencedSchemas(propertySchema, ignoreCandidates);
        }

        ignoreCandidates.forEach(name -> markIgnore(schemas, name));
    }

    private void collectReferencedSchemas(Schema<?> schema, Set<String> collected) {
        if (schema == null) {
            return;
        }

        String ref = schema.get$ref();
        if (ref != null && ref.startsWith(SCHEMA_REF_PREFIX)) {
            collected.add(ref.substring(SCHEMA_REF_PREFIX.length()));
            return;
        }

        if (schema instanceof ArraySchema arraySchema) {
            collectReferencedSchemas(arraySchema.getItems(), collected);
            return;
        }

        Schema<?> items = schema.getItems();
        if (items != null) {
            collectReferencedSchemas(items, collected);
        }
    }

    private boolean isDefaultEnvelope(ResponseTypeDescriptor descriptor) {
        return ServiceResponse.class.equals(descriptor.envelopeType());
    }

    private void markIgnore(Map<String, Schema> schemas, String schemaName) {
        Schema<?> schema = schemas.get(schemaName);
        if (schema != null) {
            schema.addExtension(VendorExtensions.IGNORE_MODEL, true);
        }
    }
}