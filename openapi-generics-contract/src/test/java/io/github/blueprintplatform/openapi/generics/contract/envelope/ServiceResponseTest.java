package io.github.blueprintplatform.openapi.generics.contract.envelope;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ServiceResponse")
class ServiceResponseTest {

    @Test
    @DisplayName("of(data) -> should create response with default meta")
    void of_withDataOnly_shouldUseDefaultMeta() {
        ServiceResponse<String> response = ServiceResponse.of("payload");

        assertEquals("payload", response.getData());
        assertNotNull(response.getMeta());
        assertNotNull(response.getMeta().serverTime());
    }

    @Test
    @DisplayName("of(data, meta) -> should preserve provided meta")
    void of_withDataAndMeta_shouldPreserveMeta() {
        Meta meta = Meta.now();
        ServiceResponse<String> response = ServiceResponse.of("payload", meta);

        assertEquals("payload", response.getData());
        assertEquals(meta, response.getMeta());
    }

    @Test
    @DisplayName("constructor -> should fall back to default meta when meta is null")
    void constructor_withNullMeta_shouldFallBackToDefault() {
        ServiceResponse<String> response = new ServiceResponse<>("payload", null);

        assertNotNull(response.getMeta());
        assertNotNull(response.getMeta().serverTime());
    }

    @Test
    @DisplayName("no-arg constructor -> should initialize meta with default")
    void noArgConstructor_shouldInitializeDefaultMeta() {
        ServiceResponse<String> response = new ServiceResponse<>();

        assertNull(response.getData());
        assertNotNull(response.getMeta());
    }

    @Test
    @DisplayName("setMeta(null) -> should fall back to default meta")
    void setMeta_withNull_shouldFallBackToDefault() {
        ServiceResponse<String> response = ServiceResponse.of("payload");
        response.setMeta(null);

        assertNotNull(response.getMeta());
    }

    @Test
    @DisplayName("setData -> should update payload")
    void setData_shouldUpdatePayload() {
        ServiceResponse<String> response = new ServiceResponse<>();
        response.setData("updated");

        assertEquals("updated", response.getData());
    }

    @Test
    @DisplayName("equals -> should be true for responses with same data and meta")
    void equals_withSameDataAndMeta_shouldReturnTrue() {
        Meta meta = Meta.now();
        ServiceResponse<String> a = ServiceResponse.of("x", meta);
        ServiceResponse<String> b = ServiceResponse.of("x", meta);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals -> should be false for different data")
    void equals_withDifferentData_shouldReturnFalse() {
        Meta meta = Meta.now();
        ServiceResponse<String> a = ServiceResponse.of("x", meta);
        ServiceResponse<String> b = ServiceResponse.of("y", meta);

        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals -> should be false for different type")
    void equals_withDifferentType_shouldReturnFalse() {
        ServiceResponse<String> response = ServiceResponse.of("x");

        assertNotEquals(response, "not a response");
        assertNotEquals(null, response);
    }

    @Test
    @DisplayName("toString -> should include data and meta")
    void toString_shouldIncludeDataAndMeta() {
        ServiceResponse<String> response = ServiceResponse.of("payload");

        String result = response.toString();

        assertTrue(result.contains("payload"));
        assertTrue(result.contains("meta"));
    }
}