package io.github.blueprintplatform.openapi.generics.contract.envelope;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.contract.paging.Sort;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: Meta")
class MetaTest {

  @Test
  @DisplayName("now() -> should create meta with current time and empty sort")
  void now_shouldCreateMetaWithEmptySort() {
    Meta meta = Meta.now();

    assertNotNull(meta.serverTime());
    assertTrue(meta.sort().isEmpty());
  }

  @Test
  @DisplayName("now(List) -> should attach provided sort list")
  void now_withSortList_shouldAttachSort() {
    List<Sort> sorts = List.of(new Sort("name", SortDirection.ASC));

    Meta meta = Meta.now(sorts);

    assertEquals(1, meta.sort().size());
    assertEquals("name", meta.sort().get(0).field());
  }

  @Test
  @DisplayName("now(List) -> should fall back to empty list when sort is null")
  void now_withNullSortList_shouldReturnEmpty() {
    Meta meta = Meta.now((List<Sort>) null);

    assertTrue(meta.sort().isEmpty());
  }

  @Test
  @DisplayName("now(varargs) -> should attach provided sort array")
  void now_withSortVarargs_shouldAttachSort() {
    Meta meta = Meta.now(new Sort("name", SortDirection.DESC));

    assertEquals(1, meta.sort().size());
    assertEquals(SortDirection.DESC, meta.sort().get(0).direction());
  }

  @Test
  @DisplayName("now(varargs) -> should fall back to empty list when sort is null")
  void now_withNullSortVarargs_shouldReturnEmpty() {
    Meta meta = Meta.now((Sort[]) null);

    assertTrue(meta.sort().isEmpty());
  }

  @Test
  @DisplayName("now(field, direction) -> should create single sort entry")
  void now_withFieldAndDirection_shouldCreateSingleSort() {
    Meta meta = Meta.now("name", SortDirection.ASC);

    assertEquals(1, meta.sort().size());
    assertEquals("name", meta.sort().get(0).field());
    assertEquals(SortDirection.ASC, meta.sort().get(0).direction());
  }

  @Test
  @DisplayName("now(field, direction) -> should return empty sort when field is null")
  void now_withNullField_shouldReturnEmptySort() {
    Meta meta = Meta.now(null, SortDirection.ASC);

    assertTrue(meta.sort().isEmpty());
  }

  @Test
  @DisplayName("now(field, direction) -> should return empty sort when field is blank")
  void now_withBlankField_shouldReturnEmptySort() {
    Meta meta = Meta.now("   ", SortDirection.ASC);

    assertTrue(meta.sort().isEmpty());
  }
}
