package io.github.blueprintplatform.openapi.generics.contract.paging;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: Page")
class PageTest {

  @Test
  @DisplayName("of -> should calculate total pages and navigation flags")
  void of_shouldCalculateTotalPagesAndFlags() {
    Page<String> page = Page.of(List.of("a", "b"), 0, 10, 25);

    assertEquals(3, page.totalPages());
    assertTrue(page.hasNext());
    assertFalse(page.hasPrev());
  }

  @Test
  @DisplayName("of -> hasPrev should be true when not on first page")
  void of_shouldSetHasPrev_whenNotFirstPage() {
    Page<String> page = Page.of(List.of("a"), 1, 10, 25);

    assertTrue(page.hasPrev());
    assertTrue(page.hasNext());
  }

  @Test
  @DisplayName("of -> hasNext should be false on last page")
  void of_shouldSetHasNextFalse_whenLastPage() {
    Page<String> page = Page.of(List.of("a"), 2, 10, 25);

    assertFalse(page.hasNext());
    assertTrue(page.hasPrev());
  }

  @Test
  @DisplayName("of(null content) -> should return empty content")
  void of_withNullContent_shouldReturnEmpty() {
    Page<String> page = Page.of(null, 0, 10, 0);

    assertTrue(page.content().isEmpty());
  }

  @Test
  @DisplayName("of -> should normalize negative page to zero")
  void of_withNegativePage_shouldNormalizeToZero() {
    Page<String> page = Page.of(List.of(), -5, 10, 0);

    assertEquals(0, page.page());
  }

  @Test
  @DisplayName("of -> should normalize zero size to one")
  void of_withZeroSize_shouldNormalizeToOne() {
    Page<String> page = Page.of(List.of(), 0, 0, 0);

    assertEquals(1, page.size());
  }

  @Test
  @DisplayName("of -> should normalize negative size to one")
  void of_withNegativeSize_shouldNormalizeToOne() {
    Page<String> page = Page.of(List.of(), 0, -5, 0);

    assertEquals(1, page.size());
  }

  @Test
  @DisplayName("of -> should return zero total pages when total elements is zero")
  void of_withZeroTotalElements_shouldReturnZeroTotalPages() {
    Page<String> page = Page.of(List.of(), 0, 10, 0);

    assertEquals(0, page.totalPages());
    assertFalse(page.hasNext());
    assertFalse(page.hasPrev());
  }

  @Test
  @DisplayName("of -> should return zero total pages when total elements is negative")
  void of_withNegativeTotalElements_shouldReturnZeroTotalPages() {
    Page<String> page = Page.of(List.of(), 0, 10, -5);

    assertEquals(0, page.totalPages());
  }

  @Test
  @DisplayName("of -> should copy content defensively")
  void of_shouldCopyContentDefensively() {
    List<String> original = new java.util.ArrayList<>(List.of("a", "b"));
    Page<String> page = Page.of(original, 0, 10, 2);

    original.add("c");

    assertEquals(2, page.content().size());
  }
}
