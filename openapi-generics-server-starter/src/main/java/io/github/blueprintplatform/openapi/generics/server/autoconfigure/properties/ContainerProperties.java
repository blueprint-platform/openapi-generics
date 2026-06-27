package io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties;

/**
 * Configuration describing a custom generic container contract.
 *
 * <p>Each configured container becomes eligible for deterministic generic reconstruction during
 * OpenAPI projection.
 *
 * @param type fully-qualified generic container class name
 * @param itemProperty JSON property containing the generic item collection
 */
public record ContainerProperties(String type, String itemProperty) {}
