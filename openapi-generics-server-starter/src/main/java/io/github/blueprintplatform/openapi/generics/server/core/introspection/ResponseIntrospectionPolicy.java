package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import java.util.Set;

public record ResponseIntrospectionPolicy(
    Class<?> envelopeType, String payloadPropertyName, Set<Class<?>> supportedContainers) {}
