package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseEntity;

class ResponseTypeIntrospectorTest {

    private final ResponseTypeIntrospector introspector = new ResponseTypeIntrospector();

    @Test
    void shouldExtractSimpleDataType() {
        ResolvableType type =
                ResolvableType.forClassWithGenerics(ServiceResponse.class, CustomerDto.class);

        Optional<String> result = introspector.extractDataRefName(type);

        assertTrue(result.isPresent());
        assertEquals("CustomerDto", result.get());
    }

    @Test
    void shouldExtractPageDataType() {
        ResolvableType pageType =
                ResolvableType.forClassWithGenerics(Page.class, CustomerDto.class);

        ResolvableType type =
                ResolvableType.forClassWithGenerics(ServiceResponse.class, pageType);

        Optional<String> result = introspector.extractDataRefName(type);

        assertTrue(result.isPresent());
        assertEquals("PageCustomerDto", result.get());
    }

    @Test
    void shouldUnwrapResponseEntity() {
        ResolvableType inner =
                ResolvableType.forClassWithGenerics(ServiceResponse.class, CustomerDto.class);

        ResolvableType type =
                ResolvableType.forClassWithGenerics(ResponseEntity.class, inner);

        Optional<String> result = introspector.extractDataRefName(type);

        assertTrue(result.isPresent());
        assertEquals("CustomerDto", result.get());
    }

    @Test
    void shouldReturnEmptyForNonServiceResponse() {
        ResolvableType type = ResolvableType.forClass(CustomerDto.class);

        Optional<String> result = introspector.extractDataRefName(type);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyForMissingGeneric() {
        ResolvableType type = ResolvableType.forClass(ServiceResponse.class);

        Optional<String> result = introspector.extractDataRefName(type);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyForUnsupportedNestedGenerics() {
        ResolvableType listType =
                ResolvableType.forClassWithGenerics(java.util.List.class, CustomerDto.class);

        ResolvableType type =
                ResolvableType.forClassWithGenerics(ServiceResponse.class, listType);

        Optional<String> result = introspector.extractDataRefName(type);

        assertTrue(result.isEmpty());
    }

    static class CustomerDto {}
}