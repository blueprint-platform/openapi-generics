package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

/**
 * Extracts contract-aware response type metadata from controller return types.
 *
 * <p>Unwraps framework-level wrappers (for example {@code ResponseEntity},
 * {@code CompletionStage}, {@code Future}, {@code DeferredResult}, and {@code WebAsyncTask})
 * before analyzing the actual contract response shape.
 *
 * <p>Produces a {@link ResponseTypeDescriptor} only for response structures that are explicitly
 * supported by the active {@link ResponseIntrospectionPolicy}.
 *
 * <p>For the default platform envelope, supported shapes are:
 *
 * <ul>
 *   <li>{@code ServiceResponse<T>}
 *   <li>{@code ServiceResponse<Page<T>>}
 *   <li>{@code ServiceResponse<List<T>>}
 * </ul>
 *
 * <p>For custom BYOE envelopes, supported shapes are limited to:
 *
 * <ul>
 *   <li>{@code YourEnvelope<T>}
 * </ul>
 *
 * <p>Nested container payloads are intentionally unsupported, including:
 *
 * <ul>
 *   <li>{@code ServiceResponse<List<List<T>>>}
 *   <li>{@code ServiceResponse<Page<List<T>>>}
 *   <li>{@code YourEnvelope<Page<T>>}
 *   <li>{@code YourEnvelope<List<T>>}
 * </ul>
 *
 * <p>Enum payloads are supported only when published as reusable OpenAPI schema components
 * (for example via {@code @Schema(enumAsRef = true)}). Inline enum schemas are ignored because
 * they do not produce stable component identities required by the projection pipeline.
 */
public final class ResponseTypeIntrospector {

  private static final Logger log = LoggerFactory.getLogger(ResponseTypeIntrospector.class);
  private static final int MAX_UNWRAP_DEPTH = 8;
  private static final String SCHEMA_ANNOTATION = "io.swagger.v3.oas.annotations.media.Schema";

  private final Class<?> envelopeType;
  private final Set<Class<?>> supportedContainers;
  private final String payloadPropertyName;

  public ResponseTypeIntrospector(ResponseIntrospectionPolicy policy) {
    this.envelopeType = policy.envelopeType();
    this.supportedContainers = policy.supportedContainers();
    this.payloadPropertyName = policy.payloadPropertyName();
  }

  public Optional<ResponseTypeDescriptor> extract(ResolvableType type) {
    type = unwrap(type);

    Class<?> raw = type.resolve();
    if (raw == null || !envelopeType.isAssignableFrom(raw)) {
      return Optional.empty();
    }

    ResolvableType dataType = type.getGeneric(0);
    Optional<ResponseTypeDescriptor> descriptorOpt = buildDescriptor(dataType);

    if (log.isDebugEnabled()) {
      log.debug(
              "Introspected type [{}]: envelopeType={}, dataType={}, descriptor={}",
              safeToString(type),
              envelopeType.getSimpleName(),
              safeToString(dataType),
              descriptorOpt.map(Object::toString).orElse("<empty>"));
    }

    return descriptorOpt;
  }

  private ResolvableType unwrap(ResolvableType type) {
    for (int i = 0; i < MAX_UNWRAP_DEPTH; i++) {
      Class<?> raw = type.resolve();
      if (raw == null || envelopeType.isAssignableFrom(raw)) {
        return type;
      }

      ResolvableType next = nextLayer(type, raw);
      if (next == null) {
        return type;
      }

      type = next;
    }

    return type;
  }

  private ResolvableType nextLayer(ResolvableType current, Class<?> raw) {
    if (ResponseEntity.class.isAssignableFrom(raw)) {
      return current.getGeneric(0);
    }

    if (CompletionStage.class.isAssignableFrom(raw) || Future.class.isAssignableFrom(raw)) {
      return current.getGeneric(0);
    }

    if (DeferredResult.class.isAssignableFrom(raw) || WebAsyncTask.class.isAssignableFrom(raw)) {
      return current.getGeneric(0);
    }

    return null;
  }
  private Optional<ResponseTypeDescriptor> buildDescriptor(ResolvableType dataType) {
    Class<?> raw = dataType.resolve();
    if (raw == null) {
      return Optional.empty();
    }

    Optional<ResponseTypeDescriptor> containerDescriptor = buildContainerDescriptor(dataType, raw);
    if (containerDescriptor.isPresent()) {
      return containerDescriptor;
    }

    if (!dataType.hasGenerics() && isSupportedPayloadType(raw)) {
      return Optional.of(
              ResponseTypeDescriptor.simple(envelopeType, payloadPropertyName, raw.getSimpleName()));
    }

    return Optional.empty();
  }

  private Optional<ResponseTypeDescriptor> buildContainerDescriptor(
          ResolvableType dataType, Class<?> raw) {

    for (Class<?> containerType : supportedContainers) {
      if (!containerType.isAssignableFrom(raw)) {
        continue;
      }

      ResolvableType itemType = safeGeneric(dataType);
      if (itemType.hasGenerics()) {
        return Optional.empty();
      }

      Class<?> itemRaw = itemType.resolve();
      if (!isSupportedPayloadType(itemRaw)) {
        return Optional.empty();
      }

      return Optional.of(
              ResponseTypeDescriptor.container(
                      envelopeType,
                      payloadPropertyName,
                      containerType.getSimpleName(),
                      itemRaw.getSimpleName()));
    }

    return Optional.empty();
  }

  private boolean isSupportedPayloadType(Class<?> type) {
    if (type == null) {
      return false;
    }

    if (!type.isEnum()) {
      return true;
    }

    return isEnumAsRefEnabled(type);
  }

  private boolean isEnumAsRefEnabled(Class<?> enumType) {
    for (Annotation annotation : enumType.getAnnotations()) {
      if (!SCHEMA_ANNOTATION.equals(annotation.annotationType().getName())) {
        continue;
      }

      try {
        Method enumAsRef = annotation.annotationType().getMethod("enumAsRef");
        Object value = enumAsRef.invoke(annotation);
        return Boolean.TRUE.equals(value);
      } catch (ReflectiveOperationException ignored) {
        return false;
      }
    }

    return false;
  }

  private ResolvableType safeGeneric(ResolvableType type) {
    if (!type.hasGenerics()) {
      return ResolvableType.NONE;
    }
    return type.getGeneric(0);
  }

  private String safeToString(ResolvableType type) {
    try {
      return String.valueOf(type);
    } catch (Exception ignored) {
      return "<unprintable>";
    }
  }
}