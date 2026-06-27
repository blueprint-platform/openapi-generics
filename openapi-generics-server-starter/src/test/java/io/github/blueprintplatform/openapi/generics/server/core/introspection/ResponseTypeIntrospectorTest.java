package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

@Tag("unit")
@DisplayName("Unit Test: ResponseTypeIntrospector")
class ResponseTypeIntrospectorTest {

  private static final SupportedContainerDescriptor PAGE_CONTAINER =
      new SupportedContainerDescriptor(
          Page.class,
          "Page",
          "Page",
          ContainerShape.OBJECT_WITH_ITEM_ARRAY,
          "content",
          ContainerSource.BUILT_IN,
          ContainerMatchMode.EXACT);

  private static final SupportedContainerDescriptor PAGING_CONTAINER =
      new SupportedContainerDescriptor(
          Paging.class,
          "Paging",
          "Paging",
          ContainerShape.OBJECT_WITH_ITEM_ARRAY,
          "content",
          ContainerSource.CONFIGURED,
          ContainerMatchMode.EXACT);

  private static final ResponseIntrospectionPolicy DEFAULT_POLICY =
      new ResponseIntrospectionPolicy(ServiceResponse.class, "data", Set.of(PAGE_CONTAINER));

  private static final ResponseIntrospectionPolicy CUSTOM_CONTAINER_POLICY =
      new ResponseIntrospectionPolicy(
          ServiceResponse.class, "data", Set.of(PAGE_CONTAINER, PAGING_CONTAINER));

  private final ResponseTypeIntrospector introspector =
      new ResponseTypeIntrospector(DEFAULT_POLICY);

  @Test
  @DisplayName("extract -> should return simple descriptor for ServiceResponse<T>")
  void extract_shouldReturnSimpleDescriptor_forSimpleEnvelope() {
    ResolvableType type =
        ResolvableType.forClassWithGenerics(
            ServiceResponse.class, ResolvableType.forClass(CustomerDto.class));

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals(ServiceResponse.class, descriptor.envelopeType());
    assertEquals("data", descriptor.payloadPropertyName());
    assertEquals("CustomerDto", descriptor.dataRefName());
    assertNull(descriptor.containerName());
    assertNull(descriptor.itemRefName());
    assertFalse(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should return container descriptor for ServiceResponse<Page<T>>")
  void extract_shouldReturnContainerDescriptor_forPageEnvelope() {
    ResolvableType pageType =
        ResolvableType.forClassWithGenerics(Page.class, ResolvableType.forClass(CustomerDto.class));

    ResolvableType type = ResolvableType.forClassWithGenerics(ServiceResponse.class, pageType);

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals(ServiceResponse.class, descriptor.envelopeType());
    assertEquals("data", descriptor.payloadPropertyName());
    assertEquals("PageCustomerDto", descriptor.dataRefName());
    assertEquals("Page", descriptor.containerName());
    assertEquals(Page.class.getName(), descriptor.containerTypeName());
    assertEquals("CustomerDto", descriptor.itemRefName());
    assertTrue(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should return container descriptor for configured container")
  void extract_shouldReturnContainerDescriptor_forConfiguredContainer() {
    ResponseTypeIntrospector customIntrospector =
        new ResponseTypeIntrospector(CUSTOM_CONTAINER_POLICY);

    ResolvableType pagingType =
        ResolvableType.forClassWithGenerics(
            Paging.class, ResolvableType.forClass(CustomerDto.class));

    ResolvableType type = ResolvableType.forClassWithGenerics(ServiceResponse.class, pagingType);

    ResponseTypeDescriptor descriptor = customIntrospector.extract(type).orElseThrow();

    assertEquals(ServiceResponse.class, descriptor.envelopeType());
    assertEquals("data", descriptor.payloadPropertyName());
    assertEquals("PagingCustomerDto", descriptor.dataRefName());
    assertEquals("Paging", descriptor.containerName());
    assertEquals(Paging.class.getName(), descriptor.containerTypeName());
    assertEquals("CustomerDto", descriptor.itemRefName());
    assertTrue(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should unwrap ResponseEntity<ServiceResponse<T>>")
  void extract_shouldUnwrapResponseEntity() {
    ResolvableType envelopeType =
        ResolvableType.forClassWithGenerics(
            ServiceResponse.class, ResolvableType.forClass(CustomerDto.class));

    ResolvableType type = ResolvableType.forClassWithGenerics(ResponseEntity.class, envelopeType);

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
    assertFalse(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should unwrap CompletionStage<ServiceResponse<T>>")
  void extract_shouldUnwrapCompletionStage() {
    ResolvableType envelopeType =
        ResolvableType.forClassWithGenerics(
            ServiceResponse.class, ResolvableType.forClass(CustomerDto.class));

    ResolvableType type =
        ResolvableType.forClassWithGenerics(CompletableFuture.class, envelopeType);

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
    assertFalse(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should unwrap Future<ServiceResponse<T>>")
  void extract_shouldUnwrapFuture() {
    ResolvableType envelopeType =
        ResolvableType.forClassWithGenerics(
            ServiceResponse.class, ResolvableType.forClass(CustomerDto.class));

    ResolvableType type = ResolvableType.forClassWithGenerics(Future.class, envelopeType);

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
    assertFalse(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should unwrap DeferredResult<ServiceResponse<T>>")
  void extract_shouldUnwrapDeferredResult() {
    ResolvableType envelopeType =
        ResolvableType.forClassWithGenerics(
            ServiceResponse.class, ResolvableType.forClass(CustomerDto.class));

    ResolvableType type = ResolvableType.forClassWithGenerics(DeferredResult.class, envelopeType);

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
    assertFalse(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should unwrap WebAsyncTask<ServiceResponse<T>>")
  void extract_shouldUnwrapWebAsyncTask() {
    ResolvableType envelopeType =
        ResolvableType.forClassWithGenerics(
            ServiceResponse.class, ResolvableType.forClass(CustomerDto.class));

    ResolvableType type = ResolvableType.forClassWithGenerics(WebAsyncTask.class, envelopeType);

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
    assertFalse(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should return empty for unsupported root type")
  void extract_shouldReturnEmpty_forUnsupportedRootType() {
    ResolvableType type = ResolvableType.forClass(CustomerDto.class);

    assertTrue(introspector.extract(type).isEmpty());
  }

  @Test
  @DisplayName("extract -> should return empty for unsupported nested generic payload")
  void extract_shouldReturnEmpty_forUnsupportedNestedGenericPayload() {
    ResolvableType nestedType =
        ResolvableType.forClassWithGenerics(
            Wrapper.class, ResolvableType.forClass(CustomerDto.class));

    ResolvableType type = ResolvableType.forClassWithGenerics(ServiceResponse.class, nestedType);

    assertTrue(introspector.extract(type).isEmpty());
  }

  @Test
  @DisplayName("extract -> should return empty for unregistered generic container")
  void extract_shouldReturnEmpty_forUnregisteredGenericContainer() {
    ResolvableType pagingType =
        ResolvableType.forClassWithGenerics(
            Paging.class, ResolvableType.forClass(CustomerDto.class));

    ResolvableType type = ResolvableType.forClassWithGenerics(ServiceResponse.class, pagingType);

    assertTrue(introspector.extract(type).isEmpty());
  }

  @Test
  @DisplayName("extract -> should return empty when container item type is unresolved")
  void extract_shouldReturnEmpty_whenContainerItemTypeUnresolved() {
    ResolvableType rawPageType = ResolvableType.forClass(Page.class);
    ResolvableType type = ResolvableType.forClassWithGenerics(ServiceResponse.class, rawPageType);

    assertTrue(introspector.extract(type).isEmpty());
  }

  private static final class CustomerDto {}

  private static final class Wrapper<T> {}

  private static final class Paging<T> {
    java.util.List<T> content;
  }
}
