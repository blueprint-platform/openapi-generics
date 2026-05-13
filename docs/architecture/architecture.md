---
layout: default
title: Architecture
nav_exclude: true
---

# Architecture — How openapi-generics Actually Works

> Internal architecture for contributors and engineers who need to understand the system beyond user-facing configuration.

This document assumes you've read the [README](../../README.md) and [Adoption Guide](../index.md). It does **not** repeat the problem statement, value proposition, or supported shapes — those are covered there.

What this document covers:

- the runtime pipeline that produces the OpenAPI projection
- the build pipeline that produces the contract-aligned client
- the vendor extension protocol that connects the two
- the design decisions that constrain what's supported, and why

---

## Contents

- [System overview](#system-overview)
- [The vendor extension protocol](#the-vendor-extension-protocol)
- [Server-side: the projection pipeline](#server-side-the-projection-pipeline)
- [Client-side: the build pipeline](#client-side-the-build-pipeline)
- [Client-side: the codegen extension](#client-side-the-codegen-extension)
- [Determinism and fail-fast points](#determinism-and-fail-fast-points)
- [Design decisions and their trade-offs](#design-decisions-and-their-trade-offs)
- [Module map](#module-map)

---

## System overview

The system has two distinct execution surfaces that never run in the same JVM:

```text
SERVER STARTUP                            CLIENT BUILD
─────────────────                         ─────────────
Spring Boot app                           mvn generate-sources
  ↓                                         ↓
Auto-configured starter                   Parent POM orchestration
  ↓                                         ↓
Pipeline orchestrator (5 steps)           Template extract → patch → overlay
  ↓                                         ↓
OpenAPI document with vendor extensions ──→ openapi-generator-maven-plugin
                                            ↓
                                          GenericAwareJavaCodegen
                                            ↓
                                          Generated Java client
```

The **only artifact crossing the boundary** is the OpenAPI document (typically served at `/v3/api-docs.yaml`). It carries:

- standard OpenAPI 3.x schemas
- a small set of `x-*` vendor extensions that encode generic semantics

Everything else — controller introspection on one side, codegen filtering on the other — runs in isolation. Server and client never share state, and the OpenAPI document is the contract between them.

---

## The vendor extension protocol

The server emits these extensions; the client consumes them. They are the **internal DSL** of the system.

| Extension | Set by | Read by | Meaning |
|---|---|---|---|
| `x-api-wrapper` | `WrapperSchemaProcessor` | `GenericAwareJavaCodegen` (templates + import resolver + envelope resolver) | This schema represents an envelope-bound wrapper. Triggers wrapper-aware template branch. |
| `x-api-wrapper-datatype` | `WrapperSchemaProcessor` | `ExternalImportResolver` (fallback) | Simple-payload case: name of the inner type (e.g. `CustomerDto` for `ServiceResponse<CustomerDto>`). |
| `x-data-container` | `WrapperSchemaProcessor` | client templates | Container case: name of the container type (e.g. `Page` for `ServiceResponse<Page<CustomerDto>>`). |
| `x-data-item` | `WrapperSchemaProcessor` | `ExternalImportResolver` (primary) | Container case: name of the item type inside the container (e.g. `CustomerDto`). |
| `x-ignore-model` | `SchemaGenerationControlMarker` | `ModelIgnoreDecider` | This schema must not be generated as a class. Suppresses envelope, `Meta`, `Sort`, container schemas. |
| `x-envelope-import` | `EnvelopeMetadataResolver` (client) | client templates | FQCN of the envelope class to import in generated wrapper. |
| `x-envelope-type` | `EnvelopeMetadataResolver` (client) | client templates | Simple name of the envelope class. |
| `x-extra-imports` | `ExternalImportResolver` | client templates | FQCN of an external (BYOC-mapped) DTO that the wrapper should import. |

The first five are produced server-side and travel inside the OpenAPI document. The last three are produced client-side during codegen and exist only as Mustache template variables.

This protocol is intentionally narrow. Everything the system needs to reconstruct generic types end-to-end is encoded in these eight extensions.

---

## Server-side: the projection pipeline

### Wiring

`OpenApiGenericsAutoConfiguration` wires the entire stack as Spring beans. Activation conditions:

```java
@AutoConfiguration
@ConditionalOnClass(OpenApiCustomizer.class)        // springdoc must be present
@ConditionalOnWebApplication                         // servlet or reactive (we filter further below)
@EnableConfigurationProperties(OpenApiGenericsProperties.class)
```

The MVC discovery strategy has an additional condition:

```java
@ConditionalOnClass(RequestMappingHandlerMapping.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
```

This is the **structural reason WebFlux is not currently supported**: the discovery strategy is a Spring MVC bean, and there is no equivalent reactive strategy registered. Adding WebFlux support would mean adding a `WebFluxResponseTypeDiscoveryStrategy` bean conditional on `RouterFunction`/`HandlerMapping` reactive equivalents — not a fundamental architectural change.

Every bean is `@ConditionalOnMissingBean`. Consumers can override any single component (introspector, marker, contract guard) by registering their own implementation.

### The five-step pipeline

`OpenApiPipelineOrchestrator.run(OpenAPI)` is invoked by springdoc as an `OpenApiCustomizer`. It executes exactly once per `OpenAPI` instance — idempotency is enforced by an identity-based `processed` set:

```java
if (!processed.add(openApi)) {
  return;
}
```

The five steps:

**1. Discovery** — `MvcResponseTypeDiscoveryStrategy.discover()`

Scans every `RequestMappingHandlerMapping` bean in the context, walks all handler methods, and collects their return types as `ResolvableType`. Types that fail to resolve are dropped silently. Output: `Set<ResolvableType>`.

This is the only step that's framework-specific. Replacing it would localize WebFlux support to one class.

**2. Introspection** — `ResponseTypeIntrospector.extract(type)`

Per discovered type:

a. Unwrap async/transport wrappers up to depth 8:

```java
ResponseEntity<T>     → T
CompletionStage<T>    → T
Future<T>             → T
DeferredResult<T>     → T
WebAsyncTask<T>       → T
```

The depth limit prevents pathological inputs from infinite-looping; it's never reached in practice (typical depth is 1–2).

b. Check `envelopeType.isAssignableFrom(raw)`. If false → return empty (this method is not contract-aware).

c. Read the first generic parameter as the data type. Match against the policy's supported containers (default: `{Page}`; BYOE: `{}`).

d. Build a `ResponseTypeDescriptor`:

- container case → `descriptor.container(envelope, payloadName, containerName, itemName)`
- simple case → `descriptor.simple(envelope, payloadName, itemName)`
- otherwise → empty

> **Important nuance**: BYOE envelopes get an empty container set. This is the structural reason `YourEnvelope<Page<T>>` is unsupported — not a validation rule, but the absence of any registered container for non-default envelopes.

**3. Schema enrichment** — `WrapperSchemaProcessor.process(openapi, descriptor)`

For each descriptor, registers (or finds) the wrapper schema in `openapi.getComponents().getSchemas()` and applies the corresponding extensions (`x-api-wrapper`, `x-api-wrapper-datatype`, `x-data-container`, `x-data-item`). Wrapper names are deterministic: envelope simple name + container name (if any) + item simple name. Same input → same output.

**4. Generation control** — `SchemaGenerationControlMarker.mark(openapi, descriptors)`

Stamps `x-ignore-model: true` on schemas that the client must **not** regenerate as classes:

- the envelope schema (e.g. `ServiceResponse`)
- contract infrastructure: `Meta`, `Sort`
- container schemas: `Page`
- any schema derived from contract types

This is the mechanism that makes the **default path** zero-config: even without BYOC mappings, the client never generates `ServiceResponse`, `Meta`, `Sort`, or `Page` because they're already imported from the `openapi-generics-contract` module.

**5. Validation** — `OpenApiContractGuard.validate(openapi, descriptors)`

Verifies the document is internally consistent: every wrapper has its required extensions, every payload reference resolves, no `x-ignore-model` markers point at non-existent schemas. Inconsistency throws `IllegalStateException` at startup. The principle: **incorrect projection is worse than no projection**.

### What this pipeline does NOT do

- it does not modify controller code
- it does not generate Java sources
- it does not validate runtime HTTP behavior
- it does not enforce error model patterns (RFC 9457 vs envelope-based — that's a service decision)

---

## Client-side: the build pipeline

The parent POM `openapi-generics-java-codegen-parent` orchestrates a five-stage build pipeline, executed during `generate-sources`:

```text
1. Extract upstream model.mustache from openapi-generator JAR
   → maven-dependency-plugin:unpack
   → output: target/effective-templates/Java/model.mustache

2. Patch the upstream template with surgical regex insertions
   → maven-antrun-plugin:run
   → injects two replacements into the {{#models}}{{#model}} loop:
       a) wraps the body in {{#vendorExtensions.x-api-wrapper}}...{{/}}
       b) routes wrapper schemas to a separate api_wrapper partial
   → verifies the patch took effect via <condition>+<fail/>; build dies if upstream changes

3. Extract our own templates from openapi-generics-java-codegen JAR
   → maven-dependency-plugin:unpack
   → output: target/codegen-templates/META-INF/openapi-generics/templates/

4. Overlay our templates onto the patched upstream
   → maven-resources-plugin:copy-resources
   → adds api_wrapper.mustache (the partial referenced by the patch)

5. Run openapi-generator-maven-plugin
   → templateDirectory points at target/effective-templates/Java
   → generatorName = java-generics-contract (resolves to GenericAwareJavaCodegen)
```

The build-helper plugin then registers the generated sources for compilation.

### Why patch-then-overlay instead of just overlay?

Drop-in templates work — but they freeze a snapshot of upstream `model.mustache` at the moment you copied it. As OpenAPI Generator evolves (7.x has had many minor releases), your snapshot drifts further behind. Behavior changes silently because your template no longer reflects what upstream actually does.

The patch approach inverts this: upstream stays as the source of structure, and we inject **only** the generic-aware branch. If upstream restructures the `{{#models}}{{#model}}` loop in a way that the regex no longer matches, the build fails immediately with:

```
OpenAPI template patch FAILED — upstream model.mustache structure changed.
```

That's the trade-off: tolerate occasional upstream-bump pain, gain durability against silent drift.

### The `openapi.generics.skip` switch

Every plugin execution that's part of the contract-aware pipeline reads `<skip>${openapi.generics.skip}</skip>`. Setting `openapi.generics.skip=true` bypasses extract/patch/overlay entirely, leaving plain `openapi-generator-maven-plugin` behavior. The generator name doesn't switch — the user does, by changing `generatorName` to a stock generator. This is intentional: the skip flag is a kill-switch for **the build pipeline**, not a behavior selector for the codegen extension.

---

## Client-side: the codegen extension

`GenericAwareJavaCodegen extends JavaClientCodegen` and registers itself under generator name `java-generics-contract` (via the OpenAPI Generator service loader). It overrides four lifecycle hooks.

### `processOpts()` — registry bootstrap

Reads two property namespaces from `additionalProperties`:

```text
openapi-generics.envelope=<FQCN>                          → EnvelopeMetadataResolver
openapi-generics.response-contract.<ModelName>=<FQCN>     → ExternalModelRegistry
```

The envelope resolver defaults to `io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse` if not configured. The model registry starts empty.

### `fromModel(name, schema)` — ignore decision

For every schema that becomes a `CodegenModel`, `ModelIgnoreDecider.shouldIgnore(name, schema)` evaluates two independent rules with OR semantics:

```java
boolean byExtension = isIgnoredByExtension(model);   // x-ignore-model=true (set server-side)
boolean byExternal  = registry.isExternal(name);     // BYOC mapping configured
return byExtension || byExternal;
```

**Both paths are always active.** The default path (no BYOC, no BYOE) relies entirely on `x-ignore-model` markers placed by the server-side `SchemaGenerationControlMarker`. BYOC adds a second filter on top. This is why even a vanilla setup never regenerates `ServiceResponse`, `Meta`, `Sort`, or `Page`.

Imports referencing ignored types are removed via `cleanImports(cm)`.

### `postProcessModels(modelsMap)` — local filtering and metadata injection

Per generator file:

1. Remove ignored models from the local list (so they're not rendered).
2. For each remaining model, call:
  - `ExternalImportResolver.apply(model)` — for wrapper models, looks up `x-data-item` (or `x-api-wrapper-datatype` as fallback) in the registry; if found, sets `x-extra-imports` to the FQCN.
  - `EnvelopeMetadataResolver.apply(model)` — for wrapper models, sets `x-envelope-import` and `x-envelope-type` to the configured envelope FQCN/simple name.

These metadata are template variables only. They never appear in the final `.java` files as text — they drive `import` statements and `extends` clauses inside `api_wrapper.mustache`.

### `postProcessAllModels(allModels)` — global graph cleanup

Removes ignored entries from the full model map. This second filtering pass exists because `postProcessModels` operates per-file, but some upstream behaviors traverse the global model graph (cross-references, oneOf resolution). Without this pass, ignored models would still appear in cross-reference chains.

### Result

For a default path with no configuration:

```java
// Generated (thin wrappers, contract imported transitively)
public class ServiceResponseCustomerDto
    extends io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse<CustomerDto> {}
```

For BYOE + BYOC:

```java
// Generated with openapi-generics.envelope=com.example.ApiResponse
//                openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto
public class ApiResponseCustomerDto
    extends com.example.ApiResponse<com.example.contract.CustomerDto> {}
```

Neither case generates the envelope class or the DTO. Both are imported from the classpath.

---

## Determinism and fail-fast points

The system is designed so that the **same input always produces the same output**, and any deviation from that contract halts immediately rather than degrading silently.

| Failure point | Where | What it catches |
|---|---|---|
| Envelope type loading | `ResponseIntrospectionPolicyResolver` (server startup) | Configured envelope FQCN can't be loaded |
| Envelope shape validation | `ResponseIntrospectionPolicyResolver` (server startup) | Not concrete / not 1 type param / not 1 payload field / nested generic payload / interface / record / enum / annotation / array / primitive / abstract |
| Contract integrity | `OpenApiContractGuard` (server, end of pipeline) | Wrapper schemas missing extensions, broken cross-references, unresolved payloads |
| Template patch | parent POM (build, after patch step) | Upstream `model.mustache` no longer matches the regex (upstream restructured) |
| Spec validation | `openapi-generator-maven-plugin` (build, default `skipValidateSpec=false`) | OpenAPI document itself is malformed |

Each failure produces an actionable error message. None falls through to "weird codegen output that compiles but misbehaves at runtime."

The deterministic-naming guarantee follows from these constraints: same envelope + same payload + same supported container set → same wrapper schema name → same generated class name. Diff stability across builds is a direct consequence.

---

## Design decisions and their trade-offs

### Restricted generic depth

Default path supports `T` and `Page<T>`. BYOE supports only `T`.

- **Why narrow**: every supported shape is a tested code path with a deterministic schema name. Arbitrary nesting (`Map<K, List<V>>`, `Either<L, R>`, etc.) requires unbounded reflection traversal and produces unstable, generator-specific naming. The current scope covers ~95% of real Spring Boot envelope use cases without opening that complexity.
- **Cost**: teams with deeper-nested envelopes can't use BYOE directly. Workaround: embed `Page<T>` semantics inside your envelope's payload type rather than wrapping `Page<T>` from outside.

### Vendor extensions over `$dynamicRef`

OpenAPI 3.1 introduced `$dynamicRef` / `$dynamicAnchor`, which can theoretically
model parametric schemas.

openapi-generics does not use them because the current toolchain does not provide
reliable end-to-end support:

- `springdoc` does not emit generic response models using `$dynamicRef`
- OpenAPI Generator 7.x does not consistently preserve `$dynamicRef` semantics
  across language generators
- Tooling support remains partial across the broader OpenAPI ecosystem

Instead, openapi-generics encodes generic semantics using a small, documented set
of `x-*` vendor extensions.

- **Benefit:** Works today with Spring Boot, springdoc, and OpenAPI Generator 7.x.
- **Cost:** The projection is not "pure" OpenAPI 3.1 generic modeling.
- **Future:** If the toolchain gains stable `$dynamicRef` support, only the
  encoding format would change; the projection and code generation architecture
  would remain the same.

### Fail-fast over silent degradation

Five named failure points (above) all throw rather than fall through.

- **Why**: a partially-correct projection is a debugging nightmare. A loud failure produces a stack trace pointing at the actual cause. A silent fallback produces "why does my client have 47 ServiceResponseXxxDto classes again?" months later.
- **Cost**: stricter adoption curve. A misconfigured envelope crashes startup. A drifted upstream template kills the build. Both are recoverable in minutes; both feel harsh on first encounter.

### Java-and-Spring-only scope

Cross-language parity isn't a goal. WebFlux isn't supported. Gradle isn't supported.

- **Why**: depth over breadth. Java generics have a specific shape (erasure + reified type tokens via reflection on `ResolvableType`) that doesn't translate to TypeScript or Python generators. Spring MVC has stable handler-mapping introspection; WebFlux has different mechanics. Maven's plugin lifecycle aligns with our extract-patch-overlay flow; Gradle would need a parallel implementation.
- **Cost**: Kotlin/Spring teams using WebFlux or Gradle are blocked. Other-language clients consuming a Java-server-emitted spec see plain OpenAPI without contract-awareness — they don't break, they just don't get the generic-preservation benefit.

### Idempotent customizer

`OpenApiPipelineOrchestrator` tracks processed `OpenAPI` instances and skips re-runs.

- **Why**: springdoc may invoke customizers multiple times depending on caching configuration. Re-running the pipeline would double-stamp extensions and corrupt the contract guard's validation.
- **Implementation**: `Collections.newSetFromMap(new IdentityHashMap<>())` — identity-based, not equality-based. Two `OpenAPI` instances with the same content but different identities each get one pipeline run.

---

## Module map

For contributors navigating the codebase:

| Module                                                                                                                                                 | Type | Responsibility |
|--------------------------------------------------------------------------------------------------------------------------------------------------------|---|---|
| [`openapi-generics-contract`](../../README.md)                                                                                                         | Runtime + build-time | Default envelope (`ServiceResponse<T>`), `Page<T>`, `Meta`, `Sort`. Imported transitively by both server starter and codegen-parent. |
| [`openapi-generics-server-starter`](../../openapi-generics-server-starter/README.md)          | Runtime | Auto-configures the projection pipeline. Loaded into Spring Boot apps that use springdoc. |
| [`openapi-generics-java-codegen`](../..//openapi-generics-java-codegen/README.md)             | Build-time | The `GenericAwareJavaCodegen` extension. Activated when the generator name is `java-generics-contract`. |
| [`openapi-generics-java-codegen-parent`](../../openapi-generics-java-codegen-parent/README.md) | Build-time (parent POM) | The orchestration layer described in [Client-side: the build pipeline](#client-side-the-build-pipeline). |
| [`openapi-generics-platform-bom`](../../openapi-generics-platform-bom/README.md)             | Build-time (BOM) | Aligned versions for all of the above. Imported in `dependencyManagement`. |

Live entry points:

- Server pipeline: `OpenApiGenericsAutoConfiguration` → `OpenApiPipelineOrchestrator`
- Client codegen: `GenericAwareJavaCodegen` (registered via `META-INF/services/org.openapitools.codegen.CodegenConfig`)
- Build orchestration: `openapi-generics-java-codegen-parent/pom.xml`, `<pluginManagement>` block

---

## Further reading

- [Adoption Guide](../index.md) — how to use this from a consumer's perspective
- [Compatibility & Support Policy](../compatibility.md) — supported version matrix
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions) — design questions, edge cases, OAS 3.1 compliance Q&A