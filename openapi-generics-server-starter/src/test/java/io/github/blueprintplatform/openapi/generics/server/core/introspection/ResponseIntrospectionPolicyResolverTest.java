package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.EnvelopeProperties;
import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.OpenApiGenericsProperties;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ResponseIntrospectionPolicyResolver")
class ResponseIntrospectionPolicyResolverTest {

    private final ResponseIntrospectionPolicyResolver resolver =
            new ResponseIntrospectionPolicyResolver();

    @Test
    @DisplayName("resolve -> should return default policy when properties are null")
    void resolve_shouldReturnDefaultPolicy_whenPropertiesNull() {
        ResponseIntrospectionPolicy policy = resolver.resolve(null);

        assertEquals(ServiceResponse.class, policy.envelopeType());
        assertEquals("data", policy.payloadPropertyName());
        assertEquals(Set.of(Page.class), policy.supportedContainers());
    }

    @Test
    @DisplayName("resolve -> should return default policy when envelope is missing")
    void resolve_shouldReturnDefaultPolicy_whenEnvelopeMissing() {
        OpenApiGenericsProperties properties = new OpenApiGenericsProperties(null);

        ResponseIntrospectionPolicy policy = resolver.resolve(properties);

        assertEquals(ServiceResponse.class, policy.envelopeType());
        assertEquals("data", policy.payloadPropertyName());
        assertEquals(Set.of(Page.class), policy.supportedContainers());
    }

    @Test
    @DisplayName("resolve -> should return default policy when envelope type is blank")
    void resolve_shouldReturnDefaultPolicy_whenEnvelopeTypeBlank() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(new EnvelopeProperties("   "));

        ResponseIntrospectionPolicy policy = resolver.resolve(properties);

        assertEquals(ServiceResponse.class, policy.envelopeType());
        assertEquals("data", policy.payloadPropertyName());
        assertEquals(Set.of(Page.class), policy.supportedContainers());
    }

    @Test
    @DisplayName("resolve -> should resolve custom envelope with direct payload field")
    void resolve_shouldResolveCustomEnvelope_whenValid() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(new EnvelopeProperties(ValidEnvelope.class.getName()));

        ResponseIntrospectionPolicy policy = resolver.resolve(properties);

        assertEquals(ValidEnvelope.class, policy.envelopeType());
        assertEquals("payload", policy.payloadPropertyName());
        assertTrue(policy.supportedContainers().isEmpty());
    }

    @Test
    @DisplayName("resolve -> should reject non fqcn envelope type")
    void resolve_shouldRejectNonFqcnEnvelopeType() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(new EnvelopeProperties("ApiResponse"));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> resolver.resolve(properties));

        assertTrue(ex.getMessage().contains("Expected fully-qualified class name"));
    }

    @Test
    @DisplayName("resolve -> should reject missing envelope class")
    void resolve_shouldRejectMissingEnvelopeClass() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(new EnvelopeProperties("com.example.DoesNotExist"));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> resolver.resolve(properties));

        assertTrue(ex.getMessage().contains("Configured envelope class not found"));
    }

    @Test
    @DisplayName("resolve -> should reject interface envelope")
    void resolve_shouldRejectInterfaceEnvelope() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(new EnvelopeProperties(InvalidEnvelopeInterface.class.getName()));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> resolver.resolve(properties));

        assertTrue(ex.getMessage().contains("must be a concrete class, not an interface"));
    }

    @Test
    @DisplayName("resolve -> should reject abstract envelope")
    void resolve_shouldRejectAbstractEnvelope() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(new EnvelopeProperties(AbstractEnvelope.class.getName()));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> resolver.resolve(properties));

        assertTrue(ex.getMessage().contains("must be a concrete class, not an abstract class"));
    }

    @Test
    @DisplayName("resolve -> should reject record envelope")
    void resolve_shouldRejectRecordEnvelope() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(new EnvelopeProperties(InvalidEnvelopeRecord.class.getName()));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> resolver.resolve(properties));

        assertTrue(ex.getMessage().contains("must be a class, not a record"));
    }

    @Test
    @DisplayName("resolve -> should reject envelope with multiple type parameters")
    void resolve_shouldRejectEnvelopeWithMultipleTypeParameters() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(new EnvelopeProperties(InvalidEnvelopeMultipleTypes.class.getName()));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> resolver.resolve(properties));

        assertTrue(ex.getMessage().contains("must declare exactly one type parameter"));
    }

    @Test
    @DisplayName("resolve -> should reject envelope without direct payload field")
    void resolve_shouldRejectEnvelopeWithoutDirectPayloadField() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(new EnvelopeProperties(InvalidEnvelopeNoPayload.class.getName()));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> resolver.resolve(properties));

        assertTrue(ex.getMessage().contains("must declare exactly one direct payload field"));
    }

    @Test
    @DisplayName("resolve -> should reject envelope with multiple direct payload fields")
    void resolve_shouldRejectEnvelopeWithMultipleDirectPayloadFields() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(
                        new EnvelopeProperties(InvalidEnvelopeMultiplePayloads.class.getName()));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> resolver.resolve(properties));

        assertTrue(ex.getMessage().contains("must declare exactly one direct payload field"));
    }

    @Test
    @DisplayName("resolve -> should reject envelope with nested payload field")
    void resolve_shouldRejectEnvelopeWithNestedPayloadField() {
        OpenApiGenericsProperties properties =
                new OpenApiGenericsProperties(new EnvelopeProperties(InvalidEnvelopeNestedPayload.class.getName()));

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> resolver.resolve(properties));

        assertTrue(ex.getMessage().contains("contains unsupported nested generic payload slot"));
    }

    static final class ValidEnvelope<T> {
        T payload;
        String status;
    }

    interface InvalidEnvelopeInterface<T> {
        T payload();
    }

    abstract static class AbstractEnvelope<T> {
        T payload;
    }

    record InvalidEnvelopeRecord<T>(T payload) {}

    static final class InvalidEnvelopeMultipleTypes<T, M> {
        T payload;
        M meta;
    }

    static final class InvalidEnvelopeNoPayload<T> {
        String status;
    }

    static final class InvalidEnvelopeMultiplePayloads<T> {
        T payload;
        T data;
    }

    static final class InvalidEnvelopeNestedPayload<T> {
        Wrapper<T> payload;
    }

    static final class Wrapper<T> {
        T value;
    }
}