package io.github.blueprintplatform.openapi.generics.contract.paging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SortDirection")
class SortDirectionTest {

  @Test
  @DisplayName("from -> should return DESC for desc values")
  void from_shouldReturnDesc() {
    assertEquals(SortDirection.DESC, SortDirection.from("desc"));
    assertEquals(SortDirection.DESC, SortDirection.from("DESC"));
    assertEquals(SortDirection.DESC, SortDirection.from("DeSc"));
  }

  @Test
  @DisplayName("from -> should return ASC for null or unknown values")
  void from_shouldReturnAsc() {
    assertEquals(SortDirection.ASC, SortDirection.from(null));
    assertEquals(SortDirection.ASC, SortDirection.from(""));
    assertEquals(SortDirection.ASC, SortDirection.from("asc"));
    assertEquals(SortDirection.ASC, SortDirection.from("ASC"));
    assertEquals(SortDirection.ASC, SortDirection.from("unknown"));
  }

  @Test
  @DisplayName("value -> should return serialized value")
  void value_shouldReturnSerializedValue() {
    assertEquals("asc", SortDirection.ASC.value());
    assertEquals("desc", SortDirection.DESC.value());
  }
}
