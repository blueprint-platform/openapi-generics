package io.github.blueprintplatform.openapi.generics.contract.paging;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: Sort")
class SortTest {

    @Test
    @DisplayName("of -> should create sort with field and direction")
    void of_shouldCreateSort() {
        Sort sort = Sort.of("name", SortDirection.DESC);

        assertEquals("name", sort.field());
        assertEquals(SortDirection.DESC, sort.direction());
    }
}