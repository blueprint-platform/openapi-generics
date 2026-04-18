package io.github.blueprintplatform.openapi.generics.server.core.introspection;

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

public final class ResponseTypeIntrospector {

  private static final Logger log = LoggerFactory.getLogger(ResponseTypeIntrospector.class);
  private static final int MAX_UNWRAP_DEPTH = 8;

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

    for (Class<?> containerType : supportedContainers) {
      if (containerType.isAssignableFrom(raw)) {
        ResolvableType itemType = safeGeneric(dataType);
        Class<?> itemRaw = itemType.resolve();
        if (itemRaw == null) {
          return Optional.empty();
        }

        return Optional.of(
            ResponseTypeDescriptor.container(
                envelopeType,
                payloadPropertyName,
                containerType.getSimpleName(),
                itemRaw.getSimpleName()));
      }
    }

    if (!dataType.hasGenerics()) {
      return Optional.of(
          ResponseTypeDescriptor.simple(envelopeType, payloadPropertyName, raw.getSimpleName()));
    }

    return Optional.empty();
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