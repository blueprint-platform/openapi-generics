package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import java.util.Set;

/**
 * Immutable configuration describing how response types should be interpreted by the
 * introspection pipeline.
 *
 * @param envelopeType envelope type to detect (e.g. ServiceResponse)
 * @param payloadPropertyName property representing the payload within the envelope
 * @param supportedContainers supported container types (e.g. Page)
 */
public record ResponseIntrospectionPolicy(
    Class<?> envelopeType, String payloadPropertyName, Set<Class<?>> supportedContainers) {}
