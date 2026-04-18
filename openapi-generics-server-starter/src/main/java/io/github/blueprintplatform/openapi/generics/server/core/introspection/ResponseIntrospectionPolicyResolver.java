package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.EnvelopeProperties;
import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.OpenApiGenericsProperties;
import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.PropertyNames;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Set;

public class ResponseIntrospectionPolicyResolver {

  public ResponseIntrospectionPolicy resolve(OpenApiGenericsProperties properties) {
    String configuredType = extractConfiguredEnvelopeType(properties);

    if (configuredType == null) {
      return new ResponseIntrospectionPolicy(
          ServiceResponse.class, PropertyNames.DATA, Set.of(Page.class));
    }

    Class<?> envelopeType = resolveExternalEnvelopeType(configuredType);
    String payloadPropertyName = validateExternalEnvelopeType(envelopeType);

    return new ResponseIntrospectionPolicy(envelopeType, payloadPropertyName, Set.of());
  }

  private String extractConfiguredEnvelopeType(OpenApiGenericsProperties properties) {
    if (properties == null) {
      return null;
    }

    EnvelopeProperties envelope = properties.envelope();
    if (envelope == null) {
      return null;
    }

    String type = envelope.type();
    return (type == null || type.isBlank()) ? null : type;
  }

  private Class<?> resolveExternalEnvelopeType(String configuredType) {
    if (configuredType == null || configuredType.isBlank()) {
      throw new IllegalStateException("Envelope type must not be null or blank");
    }

    if (!configuredType.contains(".")) {
      throw new IllegalStateException(
          "Invalid envelope type '"
              + configuredType
              + "'. Expected fully-qualified class name (e.g. com.example.ApiResponse)");
    }
    try {
      return Class.forName(configuredType);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(
          "Configured envelope class not found: '"
              + configuredType
              + "'. "
              + "Ensure the class exists and is on the application classpath.",
          e);
    }
  }

  private String validateExternalEnvelopeType(Class<?> envelopeType) {
    validateConcreteClass(envelopeType);
    TypeVariable<?> payloadTypeParameter = validateSingleTypeParameter(envelopeType);
    return validateSingleDirectPayloadSlot(envelopeType, payloadTypeParameter);
  }

  private void validateConcreteClass(Class<?> envelopeType) {
    if (envelopeType.isInterface()) {
      throw invalidEnvelope(envelopeType, "must be a concrete class, not an interface");
    } else if (envelopeType.isRecord()) {
      throw invalidEnvelope(envelopeType, "must be a class, not a record");
    } else if (envelopeType.isEnum()) {
      throw invalidEnvelope(envelopeType, "must be a class, not an enum");
    } else if (envelopeType.isAnnotation()) {
      throw invalidEnvelope(envelopeType, "must be a class, not an annotation");
    } else if (envelopeType.isArray()) {
      throw invalidEnvelope(envelopeType, "must be a class, not an array");
    } else if (envelopeType.isPrimitive()) {
      throw invalidEnvelope(envelopeType, "must be a class, not a primitive");
    } else if (Modifier.isAbstract(envelopeType.getModifiers())) {
      throw invalidEnvelope(envelopeType, "must be a concrete class, not an abstract class");
    }
  }

  private TypeVariable<?> validateSingleTypeParameter(Class<?> envelopeType) {
    TypeVariable<?>[] typeParameters = envelopeType.getTypeParameters();

    if (typeParameters.length != 1) {
      throw invalidEnvelope(envelopeType, "must declare exactly one type parameter");
    }

    return typeParameters[0];
  }

  private String validateSingleDirectPayloadSlot(
      Class<?> envelopeType, TypeVariable<?> payloadTypeParameter) {

    String payloadPropertyName = null;

    for (Field field : envelopeType.getDeclaredFields()) {
      if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
        PayloadSlotKind kind = classifyPayloadSlot(field.getGenericType(), payloadTypeParameter);

        if (kind == PayloadSlotKind.NESTED) {
          throw invalidEnvelope(
              envelopeType,
              "contains unsupported nested generic payload slot in field '"
                  + field.getName()
                  + "'");
        } else if (kind == PayloadSlotKind.DIRECT) {
          if (payloadPropertyName != null) {
            throw invalidEnvelope(
                envelopeType,
                "must declare exactly one direct payload field of type "
                    + payloadTypeParameter.getName());
          }
          payloadPropertyName = field.getName();
        }
      }
    }

    if (payloadPropertyName == null) {
      throw invalidEnvelope(
          envelopeType,
          "must declare exactly one direct payload field of type "
              + payloadTypeParameter.getName());
    }

    return payloadPropertyName;
  }

  private PayloadSlotKind classifyPayloadSlot(
      Type fieldType, TypeVariable<?> payloadTypeParameter) {
    if (fieldType instanceof TypeVariable<?> typeVariable) {
      return sameTypeVariable(typeVariable, payloadTypeParameter)
          ? PayloadSlotKind.DIRECT
          : PayloadSlotKind.NONE;
    }

    if (fieldType instanceof ParameterizedType || fieldType instanceof GenericArrayType) {
      return containsPayloadType(fieldType, payloadTypeParameter)
          ? PayloadSlotKind.NESTED
          : PayloadSlotKind.NONE;
    }

    return PayloadSlotKind.NONE;
  }

  private boolean containsPayloadType(Type type, TypeVariable<?> payloadTypeParameter) {
    if (type instanceof TypeVariable<?> typeVariable) {
      return sameTypeVariable(typeVariable, payloadTypeParameter);
    }

    if (type instanceof ParameterizedType parameterizedType) {
      for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
        if (containsPayloadType(actualTypeArgument, payloadTypeParameter)) {
          return true;
        }
      }
      return false;
    }

    if (type instanceof GenericArrayType genericArrayType) {
      return containsPayloadType(genericArrayType.getGenericComponentType(), payloadTypeParameter);
    }

    return false;
  }

  private boolean sameTypeVariable(TypeVariable<?> left, TypeVariable<?> right) {
    return left.getGenericDeclaration() == right.getGenericDeclaration()
        && left.getName().equals(right.getName());
  }

  private IllegalStateException invalidEnvelope(Class<?> envelopeType, String reason) {
    return new IllegalStateException(
        "Unsupported envelope type '" + envelopeType.getName() + "': " + reason);
  }

  private enum PayloadSlotKind {
    NONE,
    DIRECT,
    NESTED
  }
}
