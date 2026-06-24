package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.SupportedContainerType;
import java.util.Objects;

/**
 * Describes a supported response shape discovered during introspection.
 *
 * <p>For container responses, {@code dataRefName} follows the OpenAPI schema name produced by
 * springdoc, {@code containerType} preserves the Java container identity discovered during
 * introspection, and {@code itemRefName} represents the contained item schema name.
 */
public final class ResponseTypeDescriptor {

  private final Class<?> envelopeType;
  private final String payloadPropertyName;
  private final String dataRefName;
  private final SupportedContainerType containerType;
  private final String itemRefName;

  private ResponseTypeDescriptor(
      Class<?> envelopeType,
      String payloadPropertyName,
      String dataRefName,
      SupportedContainerType containerType,
      String itemRefName) {
    this.envelopeType = envelopeType;
    this.payloadPropertyName = payloadPropertyName;
    this.dataRefName = dataRefName;
    this.containerType = containerType;
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
    return new ResponseTypeDescriptor(
        envelopeType,
        payloadPropertyName,
        containerType.schemaName() + itemRefName,
        containerType,
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

  public SupportedContainerType containerType() {
    return containerType;
  }

  public String containerName() {
    return containerType != null ? containerType.containerName() : null;
  }

  public String containerTypeName() {
    return containerType != null ? containerType.containerTypeName() : null;
  }

  public String itemRefName() {
    return itemRefName;
  }

  public boolean isContainer() {
    return containerType != null && itemRefName != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ResponseTypeDescriptor that)) return false;
    return Objects.equals(envelopeType, that.envelopeType)
        && Objects.equals(payloadPropertyName, that.payloadPropertyName)
        && Objects.equals(dataRefName, that.dataRefName)
        && Objects.equals(containerType, that.containerType)
        && Objects.equals(itemRefName, that.itemRefName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(envelopeType, payloadPropertyName, dataRefName, containerType, itemRefName);
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
        + containerName()
        + '\''
        + ", containerTypeName='"
        + containerTypeName()
        + '\''
        + ", itemRefName='"
        + itemRefName
        + '\''
        + '}';
  }
}
