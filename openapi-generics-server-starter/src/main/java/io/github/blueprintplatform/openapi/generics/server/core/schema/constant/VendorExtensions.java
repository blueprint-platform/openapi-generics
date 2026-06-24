package io.github.blueprintplatform.openapi.generics.server.core.schema.constant;

/**
 * Canonical vendor extension keys used by the generics-aware OpenAPI contract.
 *
 * <p>These extensions define a <b>flat semantic layer</b> on top of OpenAPI, enabling client
 * generators to reconstruct higher-level abstractions such as:
 *
 * <ul>
 *   <li>{@code ServiceResponse<T>} (wrapper semantics)
 *   <li>{@code Page<T>} (container semantics)
 *   <li>{@code List<T>} and {@code Set<T>} (collection container semantics)
 * </ul>
 *
 * <h2>Structural model</h2>
 *
 * <ul>
 *   <li>All extensions live in a <b>flat namespace</b> inside OpenAPI schemas
 *   <li>There is no hierarchy or grouping at the specification level
 *   <li>This class reflects that reality
 * </ul>
 *
 * <h2>Ownership model</h2>
 *
 * <ul>
 *   <li>This library is the <b>sole owner</b> of these extensions
 *   <li>They are NOT part of the OpenAPI specification
 *   <li>They form a custom DSL for code generation
 * </ul>
 *
 * <h2>Design principles</h2>
 *
 * <ul>
 *   <li><b>Flat over hierarchical</b> → aligned with OpenAPI model
 *   <li><b>Deterministic</b> → same input produces same extensions
 *   <li><b>Single source of truth</b> → no duplication across classes
 *   <li><b>Preserve introspection results</b> → do not discard resolved Java container identity
 * </ul>
 *
 * <h2>Stability</h2>
 *
 * <ul>
 *   <li>Changing any key is a <b>breaking change</b>
 *   <li>Adding a new key is a backward-compatible metadata enhancement
 *   <li>Must be versioned carefully
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>
 * ServiceResponsePageCustomerDto:
 *   x-api-wrapper: true
 *   x-api-wrapper-datatype: PageCustomerDto
 *   x-data-container: Page
 *   x-data-container-type: io.github.blueprintplatform.openapi.generics.contract.paging.Page
 *   x-data-item: CustomerDto
 * </pre>
 *
 * <h2>Generation control</h2>
 *
 * <ul>
 *   <li>{@link #IGNORE_MODEL} disables model generation for a schema
 *   <li>Used for infrastructure / externally provided types
 *   <li>Schema remains in OpenAPI but is excluded from code generation
 * </ul>
 *
 * <pre>
 * ServiceResponse:
 *   x-ignore-model: true
 * </pre>
 *
 * <p>This class contains no behavior and serves purely as a centralized vocabulary.
 */
public final class VendorExtensions {

  public static final String API_WRAPPER = "x-api-wrapper";

  public static final String API_WRAPPER_DATATYPE = "x-api-wrapper-datatype";

  public static final String DATA_CONTAINER = "x-data-container";

  public static final String DATA_CONTAINER_TYPE = "x-data-container-type";

  public static final String DATA_ITEM = "x-data-item";

  public static final String IGNORE_MODEL = "x-ignore-model";

  private VendorExtensions() {}
}
