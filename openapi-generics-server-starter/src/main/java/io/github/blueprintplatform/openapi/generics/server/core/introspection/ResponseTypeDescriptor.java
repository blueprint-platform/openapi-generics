package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import java.util.Objects;

/**
 * Describes a supported response shape discovered during introspection.
 *
 * <p>For container responses, {@code dataRefName} follows the OpenAPI schema name produced by
 * springdoc, {@code container} preserves the Java container identity discovered during
 * introspection, and {@code itemRefName} represents the contained item schema name.
 */
public final class ResponseTypeDescriptor {

  private final Class<?> envelopeType;
  private final String payloadPropertyName;
  private final String dataRefName;
  private final SupportedContainerDescriptor container;
  private final String itemRefName;

  private ResponseTypeDescriptor(
      Class<?> envelopeType,
      String payloadPropertyName,
      String dataRefName,
      SupportedContainerDescriptor container,
      String itemRefName) {
    this.envelopeType = envelopeType;
    this.payloadPropertyName = payloadPropertyName;
    this.dataRefName = dataRefName;
    this.container = container;
    this.itemRefName = itemRefName;
  }

  public static ResponseTypeDescriptor simple(
      Class<?> envelopeType, String payloadPropertyName, String dataRefName) {
    return new ResponseTypeDescriptor(envelopeType, payloadPropertyName, dataRefName, null, null);
  }

  public static ResponseTypeDescriptor container(
      Class<?> envelopeType,
      String payloadPropertyName,
      SupportedContainerDescriptor container,
      String itemRefName) {
    return new ResponseTypeDescriptor(
        envelopeType,
        payloadPropertyName,
        container.schemaName() + itemRefName,
        container,
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

  public SupportedContainerDescriptor container() {
    return container;
  }

  public String containerName() {
    return container != null ? container.containerName() : null;
  }

  public String containerTypeName() {
    return container != null ? container.containerTypeName() : null;
  }

  public String itemRefName() {
    return itemRefName;
  }

  public boolean isContainer() {
    return container != null && itemRefName != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ResponseTypeDescriptor that)) return false;
    return Objects.equals(envelopeType, that.envelopeType)
        && Objects.equals(payloadPropertyName, that.payloadPropertyName)
        && Objects.equals(dataRefName, that.dataRefName)
        && Objects.equals(container, that.container)
        && Objects.equals(itemRefName, that.itemRefName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(envelopeType, payloadPropertyName, dataRefName, container, itemRefName);
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
