---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
has_toc: false
---

# Server-Side Adoption — What to Write, What the System Handles for You

This guide focuses on **what changes in your Spring Boot service** when you adopt openapi-generics. Setup steps, BYOE/BYOC configuration, and audience-by-audience entry points are covered in the [Adoption Guide](../index.md). Here we focus narrowly on:

- the controller patterns the system supports (and the ones it leaves untouched)
- what you write in your code
- how to verify the projection is working before involving any client

For internal pipeline mechanics, see [Architecture](../architecture/architecture.md).

---

## Contents

- [Where your responsibility starts and ends](#where-your-responsibility-starts-and-ends)
- [Controller patterns that work](#controller-patterns-that-work)
- [Async return types](#async-return-types)
- [Patterns the system leaves untouched](#patterns-the-system-leaves-untouched)
- [BYOE — envelope shape requirements](#byoe--envelope-shape-requirements)
- [Error responses — pick a pattern, stay consistent](#error-responses--pick-a-pattern-stay-consistent)
- [Common adoption pitfalls](#common-adoption-pitfalls)
- [Verification](#verification)

---

## Where your responsibility starts and ends

You write controllers. The starter does the rest.

You are responsible for:

- declaring controller return types using a supported envelope shape
- (optionally) configuring a custom envelope via `application.yml`
- producing error responses in a consistent pattern across the service

You are NOT responsible for:

- writing OpenAPI annotations to describe schemas
- registering Springdoc customizers
- naming or configuring wrapper schemas
- adding vendor extensions manually
- managing OpenAPI document layout

The starter inspects every controller method's return type at startup, decides whether it's a contract-aware shape, and projects it into the OpenAPI document. Methods whose return types don't match are left alone — Springdoc generates default schemas for them as if openapi-generics weren't there.

This is important: **adopting openapi-generics doesn't break your existing endpoints**. Endpoints that return non-envelope types (a plain DTO, a `Map<String, Object>`, etc.) continue to work exactly as before. The system only enriches the projection for envelope-shaped returns.

---

## Controller patterns that work

### Default envelope — single payload

```java
@GetMapping("/{id}")
public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(@PathVariable Long id) {
  return ResponseEntity.ok(ServiceResponse.of(service.findById(id)));
}
```

Generated OpenAPI:

```yaml
ServiceResponseCustomerDto:
  type: object
  x-api-wrapper: true
  x-api-wrapper-datatype: CustomerDto
  properties:
    data:
      $ref: '#/components/schemas/CustomerDto'
    meta:
      $ref: '#/components/schemas/Meta'
```

The wrapper schema is added; `ServiceResponse`, `Meta`, and `Sort` are stamped with `x-ignore-model: true` so the client doesn't regenerate them.

### Default envelope — paginated payload

```java
@GetMapping
public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(Pageable pageable) {
  return ResponseEntity.ok(ServiceResponse.of(service.findAll(pageable)));
}
```

Generated OpenAPI:

```yaml
ServiceResponsePageCustomerDto:
  type: object
  x-api-wrapper: true
  x-data-container: Page
  x-data-item: CustomerDto
  properties:
    data:
      $ref: '#/components/schemas/PageCustomerDto'
    meta:
      $ref: '#/components/schemas/Meta'
```

`Page<CustomerDto>` is recognized as a container shape. Both `Page` and the synthesized `PageCustomerDto` schema are marked ignored.

### Custom envelope (BYOE) — single payload

```yaml
# application.yml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse
```

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<CustomerDto>> getCustomer(@PathVariable Long id) {
  return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
}
```

Generated OpenAPI is the same shape as the default case — only the wrapper class name and envelope identity change. `ApiResponse` itself is stamped ignored; the client imports it from your classpath.

### Mixed return types in the same controller

```java
@RestController
public class CustomerController {

  // Contract-aware: gets wrapper schema in OpenAPI
  @GetMapping("/{id}")
  public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(@PathVariable Long id) { ... }

  // Not contract-aware: plain Springdoc schema
  @GetMapping("/health")
  public Map<String, String> health() { ... }

  // Not contract-aware: plain Springdoc schema
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) { ... }
}
```

This works without configuration. The starter only acts on methods whose unwrapped return type is assignable to the configured envelope. Everything else passes through Springdoc unchanged.

---

## Async return types

The introspector unwraps these wrapper types automatically (up to depth 8, which never matters in practice):

```java
ResponseEntity<ServiceResponse<CustomerDto>>           // unwrap 1 level
CompletionStage<ServiceResponse<CustomerDto>>          // unwrap 1 level
CompletableFuture<ServiceResponse<CustomerDto>>        // CompletionStage subtype
Future<ServiceResponse<CustomerDto>>                   // unwrap 1 level
DeferredResult<ServiceResponse<CustomerDto>>           // unwrap 1 level
WebAsyncTask<ServiceResponse<CustomerDto>>             // unwrap 1 level
ResponseEntity<CompletionStage<ServiceResponse<...>>>  // unwrap 2 levels
```

All are projected as if you'd written `ServiceResponse<CustomerDto>` directly. No additional configuration.

---

## Patterns the system leaves untouched

These return types are valid Java and produce valid OpenAPI — but they are **not contract-aware**, because they don't match a supported envelope shape. The starter leaves them alone; Springdoc generates default schemas. Your endpoints keep working; they simply don't benefit from the contract-preservation layer.

```java
// Bare DTO — no envelope
public CustomerDto getCustomer(...) { ... }
// Use ServiceResponse<CustomerDto> if you want the contract layer to apply.

// Collection — not a single envelope
public List<ServiceResponse<CustomerDto>> getCustomers(...) { ... }
// Wrap the collection inside the envelope: ServiceResponse<List<CustomerDto>>
// (when supported) or ServiceResponse<Page<CustomerDto>> for paginated reads.

// Map — not an envelope
public Map<String, ServiceResponse<CustomerDto>> getCustomers(...) { ... }
// Restructure to a single envelope; wrap the map inside the payload type.

// Nested envelope — only the outer one is checked
public ServiceResponse<Optional<CustomerDto>> getCustomer(...) { ... }
// Use ServiceResponse<CustomerDto> and let the service handle absence with HTTP 404.

// Reactive types (WebFlux)
public Mono<ServiceResponse<CustomerDto>> getCustomer(...) { ... }
// Out of scope today — see Architecture, "wiring" section, for the structural reason.
```

The first three are intentional: openapi-generics targets the **single-envelope** convention. If you have a service that returns lists or maps of envelopes, the design choice is whether to keep that as plain OpenAPI (current behavior — no change) or refactor to a single-envelope shape.

The fourth (`ServiceResponse<Optional<...>>`) is a real shape to watch for — it looks contract-aware but isn't. The starter sees `Optional` as a non-container generic and produces a plain wrapper. Use `ServiceResponse<CustomerDto>` and let your service handle absence with HTTP 404 instead.

The fifth (`Mono`/`Flux`) is the WebFlux limitation. See [Architecture — server-side wiring](../architecture/architecture.md#wiring) for the structural reason.

---

## BYOE — envelope shape requirements

If you bring your own envelope, it must satisfy strict structural constraints. **All are validated at application startup with a clear `IllegalStateException`** — there is no silent degradation.

### Required

- **Concrete class.** Not interface, not abstract, not record, not enum, not annotation, not array, not primitive.
- **Exactly one type parameter.** `ApiResponse<T>` works. `ApiResponse<T, M>` (e.g. payload + metadata both generic) does not.
- **Exactly one direct payload field of type `T`.** The field name is your choice — `data`, `payload`, `body`, `result`, anything. Static fields and synthetic fields are ignored, so `private static final String VERSION = "1.0"` is fine.
- **No nested generic payload.** A field of type `Page<T>`, `List<T>`, `Optional<T>` inside the envelope class is rejected. The payload field must be plain `T`.

### Examples

✅ Valid:

```java
public final class ApiResponse<T> {
  private T data;
  private int status;
  private String message;
  // getters/setters...
}
```

✅ Valid (different field name):

```java
public final class Envelope<T> {
  private T payload;            // any name works
  private Instant timestamp;
  // ...
}
```

❌ Rejected — abstract class:

```java
public abstract class BaseResponse<T> { ... }
// Unsupported envelope type: must be a concrete class, not an abstract class
```

❌ Rejected — record:

```java
public record ApiResponse<T>(T data) { }
// Unsupported envelope type: must be a class, not a record
```

❌ Rejected — multiple type parameters:

```java
public class ApiResponse<T, E> { private T data; private E error; }
// Unsupported envelope type: must declare exactly one type parameter
```

❌ Rejected — nested generic payload:

```java
public class ApiResponse<T> { private Page<T> data; }
// Unsupported envelope type: contains unsupported nested generic payload slot in field 'data'
```

❌ Rejected — multiple T-typed fields:

```java
public class ApiResponse<T> { private T data; private T fallback; }
// Unsupported envelope type: must declare exactly one direct payload field of type T
```

❌ Rejected — interface:

```java
public interface ApiResponse<T> { T data(); }
// Unsupported envelope type: must be a concrete class, not an interface
```

If you need a structurally complex envelope (Either/Result types, parametric metadata), this isn't the system for that case — keep using plain Springdoc for those endpoints, or wrap your complex shape into a simple `Envelope<T>` and let `T` carry the complexity.

### Why these constraints

The introspection logic uses Java reflection to walk the envelope's fields and identify the payload slot. Concrete class + single type parameter + single payload field is the shape that resolves deterministically without ambiguity. Records work the same way structurally but are rejected to keep the validation rule simple ("declared fields, not record components"). The constraints aren't arbitrary — each one closes off a class of ambiguous reflection results.

### Wrapper naming is the platform's responsibility

The platform computes wrapper schema names deterministically from the envelope and payload (e.g. `ServiceResponseCustomerDto`, `ServiceResponsePageCustomerDto`). This deterministic naming is what guarantees diff stability across builds and identical wrapper identity between server and client.

For this to hold, wrapper schemas must not be renamed by application code. If you write:

```java
@Schema(name = "MyCustomServiceResponseCustomerDto")
public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(...) { ... }
```

The annotation either gets ignored (if the starter wins the ordering race) or produces a wrapper with mismatched extensions (if the annotation wins). Either outcome breaks deterministic naming, so the platform expects wrapper naming to remain its responsibility.

For non-wrapper concerns — `@Operation`, `@Parameter`, request body annotations, summary/description text — Springdoc annotations work normally. The platform only owns wrapper schema *identity*.

### Extending the OpenAPI document

The starter registers exactly one customizer (`openApiGenericsCustomizer`) for envelope projection. You can register additional `OpenApiCustomizer` beans for unrelated concerns — info block, server URLs, security schemes, tag descriptions — and they compose normally with the platform.

The constraint is narrow: customizers should not mutate schemas already marked `x-api-wrapper`. The contract guard validates the document at the end of the pipeline, and any inconsistency in wrapper schemas surfaces as a startup failure. This is a feature, not a footgun: misaligned wrappers fail at boot, not in the client months later.

---

## Error responses — pick a pattern, stay consistent

The starter projects success envelopes. It does **not** prescribe an error shape. You choose, but you must be consistent within a service.

### Pattern A — RFC 9457 ProblemDetail (recommended for new services)

```java
@ExceptionHandler(CustomerNotFoundException.class)
public ResponseEntity<ProblemDetail> handle(CustomerNotFoundException e) {
  ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  pd.setType(URI.create("https://example.com/errors/customer-not-found"));
  pd.setTitle("Customer Not Found");
  return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .body(pd);
}
```

OpenAPI will document `application/problem+json` responses separately from the success `ServiceResponse<T>` schema. Generated clients see two distinct response types per operation.

When to choose this:
- new service with no legacy clients
- you want HTTP-native error semantics (Accept negotiation, RFC 7807 tooling)
- error responses don't need to share shape with success responses

### Pattern B — Envelope-based errors

```java
@ExceptionHandler(CustomerNotFoundException.class)
public ResponseEntity<ServiceResponse<Void>> handle(CustomerNotFoundException e) {
  return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(ServiceResponse.error(List.of(
          new ApiError("CUSTOMER_NOT_FOUND", e.getMessage())
      )));
}
```

OpenAPI projects success and error responses with the same envelope wrapper. Generated clients deserialize both branches into the same type; client-side code branches on the presence of `errors`/`data`.

When to choose this:
- existing clients already parse a uniform envelope
- shared error-handling middleware depends on envelope shape
- migrating to ProblemDetail would break consumers you don't control

### Stay consistent within a service

Mixing patterns within a single service multiplies error-parsing surface area: clients end up writing two error parsers, and every consumer team rediscovers this on first failure. The starter doesn't enforce consistency — that's a service-level discipline. Pick one pattern at the start of a service's life and apply it across all error paths.

The other shape worth avoiding is putting error information inside a *successful* envelope's payload — `ServiceResponse<CustomerDto>` with `data: null` and an `errors` array, returned with HTTP 200. This combination confuses HTTP-status-aware tooling and forces every consumer to re-examine the body to discover failures. Use HTTP status codes for success/failure routing; use the envelope (or `ProblemDetail`) for shape.

---

## Common adoption pitfalls

These aren't system failures — they're shapes that look right but produce surprising results during early adoption. None of them break the build silently; the platform catches the most consequential ones at startup. The notes here help you recognize each shape sooner.

### Custom OpenAPI document path

If your service publishes the OpenAPI document at a non-default path:

```yaml
springdoc:
  api-docs:
    path: /api/v3/api-docs.yaml
```

The client module must be told where to fetch it from. Mismatches between the producer's published path and the client's `inputSpec` produce a generated client that compiles against an empty contract — no compilation error, no runtime warning, just an empty `dto/` folder and broken consumers.

When you change the path on the server, update every client module that consumes the spec. Treat the spec URL as part of the contract surface.

### Springdoc version below the supported baseline

springdoc-openapi 2.7.x and earlier had different `OpenApiCustomizer` invocation semantics that don't compose correctly with the starter's idempotency check. The supported baseline is 2.8.x for Spring Boot 3.x and 3.x for Spring Boot 4.x — see [Compatibility](../compatibility.md).

If you're locked to an older springdoc version for unrelated reasons, the platform won't fail loudly at startup, but the projection guarantees no longer hold. Upgrade or stay below the contract-aware boundary; don't run the starter against an unsupported springdoc.

### BYOE class on the producer's classpath but not the contract module

When you configure a custom envelope via `application.yml`, the envelope class must be loadable by the producer service. Most teams put the envelope in a shared `*-contract` module (alongside the DTOs); this module is then imported by both producer and client.

A common mistake is to put the envelope class only in the producer module — the producer starts up fine, but the client module can't resolve it for `extends ApiResponse<CustomerDto>` and the client build fails with `cannot find symbol`. The fix is structural: keep BYOE envelopes in the shared contract module, not the producer.

---

## Verification

After adding the starter and writing your first envelope-returning endpoint, verify in this order:

### 1. Application starts

If the application fails to start with `IllegalStateException: Unsupported envelope type ...`, your BYOE configuration is wrong. The error message names the specific constraint violated. Fix the envelope or remove the `openapi-generics.envelope` property to fall back to default `ServiceResponse<T>`.

### 2. Endpoint serves the expected shape

```bash
curl -s http://localhost:8084/customer-service/v1/customers/1 | jq
```

Default envelope:

```json
{
  "data": { "customerId": 1, "name": "Acme Corp" },
  "meta": { "timestamp": "2026-05-03T..." }
}
```

Custom envelope (BYOE) — your envelope shape, e.g.:

```json
{
  "data": { "customerId": 1, "name": "Acme Corp" },
  "status": 200,
  "message": "OK"
}
```

If the runtime shape is wrong, openapi-generics didn't cause it — your envelope's serialization (Jackson configuration, getters, `@JsonProperty`) is the issue. Fix at the contract module level.

### 3. OpenAPI document contains wrapper extensions

```bash
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml | grep -A2 "x-api-wrapper"
```

Expected output:

```yaml
      x-api-wrapper: true
      x-api-wrapper-datatype: CustomerDto
      x-data-container: Page
      x-data-item: CustomerDto
```

If extensions are missing, the controller method's return type didn't match the envelope. Check it's `ResponseEntity<ServiceResponse<CustomerDto>>` (or your BYOE equivalent), not a bare DTO or a wrapped collection.

### 4. Envelope and infrastructure schemas are marked ignored

```bash
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml | grep -B1 "x-ignore-model"
```

Expected: `ServiceResponse`, `Meta`, `Sort`, `Page` (or your envelope class name) all carry `x-ignore-model: true`. If they don't, the client will regenerate them — the projection succeeded but the generation control step didn't run. This usually means an exception was swallowed during pipeline execution; check application startup logs.

### 5. Client compiles

The final verification is generating a client and confirming wrappers extend the expected envelope. Detailed in [Client-Side Adoption](./client-side-adoption.md).

If all five steps pass, server-side adoption is complete. The contract is published correctly; everything downstream depends on the OpenAPI document, not on your service code.