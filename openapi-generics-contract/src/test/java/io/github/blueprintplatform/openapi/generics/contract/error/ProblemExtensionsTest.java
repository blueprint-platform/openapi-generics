package io.github.blueprintplatform.openapi.generics.contract.error;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ProblemExtensions")
class ProblemExtensionsTest {

  @Test
  @DisplayName("ofErrors -> should wrap error items")
  void ofErrors_shouldWrapItems() {
    List<ErrorItem> items = List.of(new ErrorItem("E1", "msg", "field", "resource", "id"));

    ProblemExtensions extensions = ProblemExtensions.ofErrors(items);

    assertEquals(1, extensions.errors().size());
    assertEquals("E1", extensions.errors().get(0).code());
  }

  @Test
  @DisplayName("ofErrors -> should accept empty list")
  void ofErrors_shouldAcceptEmptyList() {
    ProblemExtensions extensions = ProblemExtensions.ofErrors(List.of());

    assertTrue(extensions.errors().isEmpty());
  }
}
