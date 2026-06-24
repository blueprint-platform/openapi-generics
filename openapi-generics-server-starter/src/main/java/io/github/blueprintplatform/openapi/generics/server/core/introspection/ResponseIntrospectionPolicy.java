package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.SupportedContainerType;
import java.util.Set;

/**
 * Immutable configuration describing how response types should be interpreted by the introspection
 * pipeline.
 *
 * <p>This policy defines:
 *
 * <ul>
 *   <li>Which envelope type should be recognized during response analysis
 *   <li>Which property inside the envelope represents the payload
 *   <li>Which container types are supported for generic reconstruction
 * </ul>
 *
 * <p>For the default platform configuration this typically corresponds to:
 *
 * <pre>{@code
 * ServiceResponse<T>
 * ServiceResponse<Page<T>>
 * ServiceResponse<List<T>>
 * }</pre>
 *
 * <p>Additional containers may be contributed through the {@link SupportedContainerType} model
 * without changing the core introspection algorithm.
 *
 * @param envelopeType envelope type to detect (for example {@code ServiceResponse})
 * @param payloadPropertyName property representing the payload within the envelope
 * @param supportedContainers supported generic container definitions
 */
public record ResponseIntrospectionPolicy(
    Class<?> envelopeType,
    String payloadPropertyName,
    Set<SupportedContainerType> supportedContainers) {}
