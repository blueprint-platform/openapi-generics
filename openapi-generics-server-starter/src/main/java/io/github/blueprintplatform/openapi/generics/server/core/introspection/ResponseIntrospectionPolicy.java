package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import java.util.Set;

/**
 * Defines the active response introspection policy.
 *
 * @param envelopeType active response envelope type
 * @param payloadPropertyName JSON property carrying the envelope payload
 * @param supportedContainers supported generic container contracts
 */
public record ResponseIntrospectionPolicy(
    Class<?> envelopeType,
    String payloadPropertyName,
    Set<SupportedContainerDescriptor> supportedContainers) {}
