package io.github.blueprintplatform.samples.typecoverage.contract;

import java.util.List;

/**
 * User-owned paged container contract used to verify BYOC container reconstruction.
 *
 * @param content current page items
 * @param page zero-based page index
 * @param size requested page size
 * @param totalElements total available element count
 * @param totalPages total available page count
 * @param hasNext whether another page exists
 * @param <T> item type
 */
public record Paging<T>(
    List<T> content, int page, int size, long totalElements, int totalPages, boolean hasNext) {

  public Paging {
    content = content == null ? List.of() : List.copyOf(content);
  }

  public static <T> Paging<T> of(List<T> content, int page, int size, long totalElements) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.max(size, 1);

    long totalPagesLong = totalElements <= 0L ? 0L : ((totalElements + safeSize - 1L) / safeSize);

    int totalPages = totalPagesLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totalPagesLong;

    boolean hasNext = safePage < totalPages - 1;

    return new Paging<>(content, safePage, safeSize, totalElements, totalPages, hasNext);
  }
}
