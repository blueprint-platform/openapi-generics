package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.SupportedContainerType;

import java.util.Objects;

/**
 * Describes a supported response shape discovered during introspection.
 *
 * <p>For container responses, {@code dataRefName} follows the OpenAPI schema name produced by
 * springdoc, while {@code containerName} represents the semantic container identifier used by
 * OpenAPI Generics vendor extensions.
 */
public final class ResponseTypeDescriptor {

  private final Class<?> envelopeType;
  private final String payloadPropertyName;
  private final String dataRefName;
  private final String containerName;
  private final String itemRefName;

  private ResponseTypeDescriptor(
          Class<?> envelopeType,
          String payloadPropertyName,
          String dataRefName,
          String containerName,
          String itemRefName) {
    this.envelopeType = envelopeType;
    this.payloadPropertyName = payloadPropertyName;
    this.dataRefName = dataRefName;
    this.containerName = containerName;
    this.itemRefName = itemRefName;
  }

  public static ResponseTypeDescriptor simple(
          Class<?> envelopeType, String payloadPropertyName, String dataRefName) {
    return new ResponseTypeDescriptor(envelopeType, payloadPropertyName, dataRefName, null, null);
  }

  public static ResponseTypeDescriptor container(
          Class<?> envelopeType,
          String payloadPropertyName,
          SupportedContainerType containerType,
          String itemRefName) {

    String dataRefName = containerType.schemaName() + itemRefName;

    return new ResponseTypeDescriptor(
            envelopeType,
            payloadPropertyName,
            dataRefName,
            containerType.containerName(),
            itemRefName);
  }

  public Class<?> envelopeType() {
    return envelopeType;
  }

  public String payloadPropertyName() {
    return payloadPropertyName;
  }

  public String dataRefName() {
    return dataRefName;
  }

  public String containerName() {
    return containerName;
  }

  public String itemRefName() {
    return itemRefName;
  }

  public boolean isContainer() {
    return containerName != null && itemRefName != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ResponseTypeDescriptor that)) return false;
    return Objects.equals(envelopeType, that.envelopeType)
            && Objects.equals(payloadPropertyName, that.payloadPropertyName)
            && Objects.equals(dataRefName, that.dataRefName)
            && Objects.equals(containerName, that.containerName)
            && Objects.equals(itemRefName, that.itemRefName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(envelopeType, payloadPropertyName, dataRefName, containerName, itemRefName);
  }

  @Override
  public String toString() {
    return "ResponseTypeDescriptor{"
            + "envelopeType="
            + (envelopeType != null ? envelopeType.getSimpleName() : "null")
            + ", payloadPropertyName='"
            + payloadPropertyName
            + '\''
            + ", dataRefName='"
            + dataRefName
            + '\''
            + ", containerName='"
            + containerName
            + '\''
            + ", itemRefName='"
            + itemRefName
            + '\''
            + '}';
  }
}