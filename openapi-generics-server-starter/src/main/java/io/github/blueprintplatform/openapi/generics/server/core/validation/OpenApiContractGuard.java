package io.github.blueprintplatform.openapi.generics.server.core.validation;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions.API_WRAPPER;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions.API_WRAPPER_DATATYPE;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions.DATA_CONTAINER;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions.DATA_ITEM;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenApiContractGuard {

  private static final Logger log = LoggerFactory.getLogger(OpenApiContractGuard.class);

  public void validate(OpenAPI openApi, Set<ResponseTypeDescriptor> descriptors) {
    log.debug("OpenAPI contract validation started");

    Map<String, Schema> schemas = getSchemas(openApi);
    validateDescriptors(schemas, descriptors);

    log.debug("OpenAPI contract validation completed successfully");
  }

  private void validateDescriptors(
      Map<String, Schema> schemas, Set<ResponseTypeDescriptor> descriptors) {
    if (descriptors == null || descriptors.isEmpty()) {
      return;
    }

    for (ResponseTypeDescriptor descriptor : descriptors) {
      validateEnvelopeSchemaExists(schemas, descriptor);
      validateWrapperSchema(schemas, descriptor);
    }
  }

  private void validateEnvelopeSchemaExists(
      Map<String, Schema> schemas, ResponseTypeDescriptor descriptor) {

    String envelopeSchemaName = descriptor.envelopeType().getSimpleName();

    if (!schemas.containsKey(envelopeSchemaName)) {
      failMissingSchema("envelope", envelopeSchemaName);
    }
  }

  private void validateWrapperSchema(
      Map<String, Schema> schemas, ResponseTypeDescriptor descriptor) {

    String wrapperName = descriptor.envelopeType().getSimpleName() + descriptor.dataRefName();
    Schema<?> wrapper = schemas.get(wrapperName);

    if (wrapper == null) {
      failMissingSchema("wrapper", wrapperName);
    }

    validateWrapperExtensions(wrapperName, wrapper, descriptor);
    validateWrapperStructure(wrapperName, wrapper, descriptor);

    if (descriptor.isContainer()) {
      validateContainerExtensions(wrapperName, wrapper, descriptor);
    }
  }

  private void validateWrapperExtensions(
      String wrapperName, Schema<?> wrapper, ResponseTypeDescriptor descriptor) {

    Map<String, Object> extensions = requireExtensions(wrapperName, wrapper, "required extensions");

    requireTrueExtension(wrapperName, extensions, API_WRAPPER);
    requireMatchingExtension(
        wrapperName, extensions, API_WRAPPER_DATATYPE, descriptor.dataRefName());
  }

  private void validateWrapperStructure(
      String wrapperName, Schema<?> wrapper, ResponseTypeDescriptor descriptor) {

    if (wrapper.getAllOf() == null || wrapper.getAllOf().isEmpty()) {
      failInvalidStructure(wrapperName, "missing allOf composition");
    }

    boolean hasPayloadProperty =
        wrapper.getAllOf().stream()
            .filter(schema -> schema.getProperties() != null)
            .anyMatch(
                schema -> schema.getProperties().containsKey(descriptor.payloadPropertyName()));

    if (!hasPayloadProperty) {
      failMissingProperty(wrapperName, descriptor.payloadPropertyName());
    }
  }

  private void validateContainerExtensions(
      String wrapperName, Schema<?> wrapper, ResponseTypeDescriptor descriptor) {

    Map<String, Object> extensions =
        requireExtensions(wrapperName, wrapper, "required container extensions");

    requireMatchingExtension(wrapperName, extensions, DATA_CONTAINER, descriptor.containerName());
    requireMatchingExtension(wrapperName, extensions, DATA_ITEM, descriptor.itemRefName());
  }

  private Map<String, Schema> getSchemas(OpenAPI openApi) {
    if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
      log.error("OpenAPI validation failed: components.schemas is missing");
      throw new IllegalStateException("OpenAPI components.schemas is missing");
    }

    return openApi.getComponents().getSchemas();
  }

  private Map<String, Object> requireExtensions(
      String wrapperName, Schema<?> wrapper, String detail) {

    Map<String, Object> extensions = wrapper.getExtensions();
    if (extensions == null) {
      failMissingExtensions(wrapperName, detail);
    }

    return extensions;
  }

  private void requireTrueExtension(
      String wrapperName, Map<String, Object> extensions, String extensionName) {

    Object actual = extensions.get(extensionName);
    if (!Boolean.TRUE.equals(actual)) {
      failInvalidExtension(wrapperName, extensionName, Boolean.TRUE, actual);
    }
  }

  private void requireMatchingExtension(
      String wrapperName,
      Map<String, Object> extensions,
      String extensionName,
      Object expectedValue) {

    Object actualValue = extensions.get(extensionName);
    if (!expectedValue.equals(actualValue)) {
      failInvalidExtension(wrapperName, extensionName, expectedValue, actualValue);
    }
  }

  private void failMissingSchema(String schemaType, String schemaName) {
    log.error("Missing {} schema '{}'", schemaType, schemaName);
    throw new IllegalStateException(
        "Missing required " + schemaType + " schema: '" + schemaName + "'");
  }

  private void failMissingExtensions(String wrapperName, String detail) {
    log.error("Wrapper '{}' missing {}", wrapperName, detail);
    throw new IllegalStateException("Wrapper schema '" + wrapperName + "' is missing " + detail);
  }

  private void failInvalidExtension(
      String wrapperName, String extensionName, Object expectedValue, Object actualValue) {

    log.error(
        "Wrapper '{}' has invalid '{}': expected '{}', actual '{}'",
        wrapperName,
        extensionName,
        expectedValue,
        actualValue);

    throw new IllegalStateException(
        "Wrapper schema '" + wrapperName + "' has invalid extension: " + extensionName);
  }

  private void failInvalidStructure(String wrapperName, String detail) {
    log.error("Wrapper '{}' has invalid structure: {}", wrapperName, detail);
    throw new IllegalStateException(
        "Wrapper schema '" + wrapperName + "' must use allOf composition");
  }

  private void failMissingProperty(String wrapperName, String propertyName) {
    log.error("Wrapper '{}' missing required property '{}'", wrapperName, propertyName);
    throw new IllegalStateException(
        "Wrapper schema '" + wrapperName + "' must define '" + propertyName + "' property");
  }
}