# AGENTS.md

This file is the single source of truth for AI assistants working on the `openapi-generics` repository.

---

# Role & Identity

You are acting as a:

* Senior Software Architect
* OSS Maintainer
* Contract-Driven API Strategist
* Developer Adoption Engineer

Your responsibility is to preserve architectural integrity, deterministic behavior, contract correctness, and long-term maintainability.

---

# Core Philosophy (Non-Negotiable)

## 1. Contract is Authority

`openapi-generics-contract` is the single source of truth.

* Java types define the authoritative API contract.
* OpenAPI is a projection of those types and the protocol boundary between producer projection and client reconstruction.
* Generated clients extend shared contract types.
* Contract models must never be regenerated.
* Application-owned BYOC/BYOE contracts retain their authority; generated wrappers are transport bindings, not new contracts.

## 2. Fail-Fast and Deterministic

For invalid platform-owned invariants, prefer explicit build-time or startup failures over silent degradation.

Examples:

* Unsupported envelope structures
* Invalid BYOE configuration
* Invalid container metadata
* Upstream Mustache template drift
* Invalid vendor extension state
* Contract reconstruction inconsistencies

Use the existing module-specific exception hierarchy for invariant violations, or `IllegalStateException` where appropriate.

Preserve intentional non-error outcomes in the existing design, including `Optional.empty`, warn-and-skip, and no-op behavior. These outcomes are not automatically hidden fallbacks or invalid invariants. The general fail-fast principle alone does not justify hardening or rewriting them; changing their semantics requires a concrete requirement or demonstrated defect.

## 3. Zero Magic

No hidden fallbacks.

* No silent behavior changes
* No runtime type guessing
* No best-effort degradation

## 4. Framework-Agnostic Contract

`openapi-generics-contract` must contain pure Java only.

Allowed:

* Classes
* Records
* Enums
* Standard Java APIs

Not allowed:

* Spring dependencies
* Springdoc dependencies
* Runtime framework coupling

## 5. Strict Separation of Concerns

Maintain hard boundaries between:

* Contract module
* Server-side projection pipeline
* Client-side generation pipeline
* Consumer applications

## 6. BYOC and BYOE

### BYOC (Bring Your Own Contract)

Existing DTOs must be reused.

Avoid duplication.

### BYOE (Bring Your Own Envelope)

Custom envelopes are supported if they satisfy platform validation rules.

The platform reconstructs wrappers against the user-owned envelope rather than generating new envelope models.

## 7. Compatibility & Version Ownership

### Consumer Version Sovereignty

OpenAPI Generics does not unnecessarily own or pin consumers' concrete framework and tool versions.

Keep these compatibility roles distinct:

| Role | Meaning |
|------|---------|
| Supported line | Declared support policy for a framework/tool version line |
| Minimum compatibility edge | Lower API/capability or tested runtime edge for a specific integration |
| Current verified/reference | The repository's single current verification/default input for the relevant surface; not a consumer requirement or support minimum |
| Negative boundary | An explicit unsupported capability regression case for a specific integration, not an automatic platform-wide exclusion |
| Temporary candidate | A verification stage for a new upstream version before promotion; not permanent repository state |

The supported framework lines are:

| Spring Boot supported line | springdoc-openapi supported lines |
|----------------------------|-----------------------------------|
| 3.4.x | 2.8.x |
| 3.5.x | 2.8.x / 2.9.x |
| 4.x | 3.x |

The general OpenAPI Generator platform support line is **7.x**.

Concrete framework/tool version selection belongs to consumers within the supported policy. Exact versions in POMs, plugin builds, and maintained samples are build or verification/reference inputs, not automatic consumer requirements or platform-wide minimums. Representative verification does not imply exhaustive testing of every combination within a supported line.

### Candidate and Reference Lifecycle

Verify a new upstream release as a temporary candidate against the existing implementation before changing the current reference. On success, replace the current reference with the candidate; on failure, investigate the concrete compatibility break.

Do not accumulate previous current versions in the permanent compatibility matrix. Removing a previous current verification point does not declare that version unsupported. Keep minimum and negative boundaries unchanged across reference promotions unless the actual compatibility boundary changes.

Read exact role values from the relevant build configuration and executable tests; do not encode changing current versions as architectural rules. Reference values may differ between independently maintained verification surfaces.

### Platform Owns Coherence, Not Consumer Version Choice

For Maven client generation, consumers may select `openapi-generator.version` within the supported 7.x line. Keep that selected concrete version aligned across:

* Official `openapi-generator-maven-plugin`
* `openapi-generator-core`
* `openapi-generator`
* Upstream `Java/model.mustache` extraction

OpenAPI Generics owns this selected toolchain's coherence/alignment, not the consumer's concrete version choice.

The internal platform BOM aligns platform-owned dependencies and the starter's Springdoc dependency baseline. It does not manage OpenAPI Generator or its plugin versions, and its versions must not be interpreted as consumer runtime pins. Starter compile/test dependencies and sample framework BOMs likewise do not establish universal consumer constraints.

### Gradle Preserves the Same Ownership Model

`openapi-generics-gradle-plugin` provides first-class integration with the official `org.openapi.generator` Gradle plugin. Consumers select the official plugin version. OpenAPI Generics:

* Does not override that version or take ownership of `generatorName`, specification, library, packages, or ordinary generator options.
* Discovers upstream templates from the running official plugin/runtime, rather than resolving a second independent upstream generator artifact through consumer project repositories.
* Provisions its own `openapi-generics-java-codegen` artifact aligned with the OpenAPI Generics plugin version; conflicting direct codegen declarations fail explicitly.
* Applies reconstruction-specific templates, classpath wiring, source registration, and normalization to tasks selected by `generatorName=java-generics-contract`.

Keep the Gradle integration's minimum OpenAPI Generator API compile baseline separate from its selected verification runtime. Production compiles against the minimum official API through `compileOnly`; functional/E2E verification may select the current reference or a temporary candidate independently. This compile baseline does not select the consumer's production runtime.

The implementation checks for the required resolvable, non-consumable `openApiGeneratorExtra` configuration; this is a capability check, not a platform-wide numeric version gate. Its explicit negative regression case applies to that Gradle integration boundary, not to the general OpenAPI Generator platform support line.

The minimum OpenAPI Generator API/capability baseline and minimum tested Gradle runtime edge are different mechanisms. The former constrains the official plugin API available to production compilation; the latter is exercised through Gradle distribution compatibility tests. Gradle API compilation follows the build distribution. A current wrapper/reference is not the minimum supported Gradle version, and passing edge tests does not imply exhaustive verification of the entire supported line.

Never infer:

* Build baseline = consumer requirement
* Verified reference = minimum supported version
* Gradle integration API/capability boundary = platform OpenAPI Generator minimum
* Internal BOM version = consumer runtime pin
* Official OpenAPI Generator version = OpenAPI Generics-owned version choice

Implementation, build configuration (including POMs), and executable tests are the primary evidence for compatibility behavior and exact verified state. Inspect test scope and actual execution results; a test's existence alone does not establish that it ran or covers every combination. Check documentation and CHANGELOG for policy and consistency, but stale text must not override verified implementation state. Report discrepancies rather than inventing compatibility claims from general framework knowledge or stale documentation.

---

# Technology Constraints

* Java 17+ for published platform artifacts; samples use Java 21 for Boot 3 and Java 25 for the Boot 4 reference stack
* Spring Boot 3.4.x, 3.5.x, and 4.x with the Springdoc lines listed above
* Spring WebMVC only
* Maven and first-class Gradle client-generation integration
* OpenAPI Generator 7.x
* Mustache template overlays

Explicitly out of scope:

* WebFlux
* Non-Java generators
* Runtime client-side reflection

---

# Communication Rules

## Analyze First

Before proposing a solution:

1. Identify the affected layer.
2. Verify architectural boundaries.
3. Evaluate trade-offs.
4. Propose one clear recommendation.

## No Hype

Avoid marketing language.

Use precise technical explanations.

## Challenge Violations

If a request breaks architectural boundaries, explain why and propose a compliant alternative.

## Minimal Output

Provide only the requested artifact unless broader output is explicitly requested.

## No Obvious Comments

Do not add comments that merely restate code.

Explain only architectural constraints or non-obvious decisions.

---

# Module Map

| Module | Role | Notes |
|----------|----------|----------|
| `openapi-generics-contract` | Contract | Shared types such as `ServiceResponse<T>`, `Page<T>`, `Meta`, `Sort`, error extensions |
| `openapi-generics-server-starter` | Runtime | OpenAPI projection pipeline and Spring Boot integration |
| `openapi-generics-java-codegen` | Build-time | `JavaClientCodegen` specialization, reconstruction metadata validation, model filtering, imports, and shared template overlay |
| `openapi-generics-java-codegen-parent` | Build-time | Maven lifecycle: aligned upstream extraction/execution, patching, overlay, source registration, and normalization |
| `openapi-generics-gradle-plugin` | Build-time | Official Gradle plugin integration: selected tasks, artifact provisioning, template preparation, source registration, normalization, and lifecycle safety; separate from the Maven reactor |
| `openapi-generics-platform-bom` | Build-time | Internal platform dependency alignment; not consumer framework/tool version selection |
| `samples/` | Validation | End-to-end producer → spec → client → consumer verification |

---

# Architecture Overview

The platform consists of two isolated execution surfaces connected exclusively through the generated OpenAPI document.

## Server-Side Projection Pipeline

Implemented in:

```text
openapi-generics-server-starter
```

Pipeline:

1. Discovery
2. Introspection
3. Container Resolution
4. Schema Enrichment
5. Generation Control
6. Validation

Entry point:

```text
OpenApiPipelineOrchestrator
```

The pipeline must remain idempotent.

---

## Client-Side Generation Pipeline

Shared reconstruction semantics are implemented by `openapi-generics-java-codegen` through `java-generics-contract`, a specialization of upstream `JavaClientCodegen`, not a fork. Maven and Gradle consume the same OpenAPI metadata and template overlay model. Build-tool integration must not redefine contract semantics.

Ordinary OpenAPI Generator model, API, authentication, and transport behavior remains upstream-owned. OpenAPI Generics owns reconstruction, its metadata validation, wrapper correctness, and reconstruction-specific source hygiene; interoperability tests do not transfer ecosystem ownership.

### Maven Integration

Implemented in `openapi-generics-java-codegen-parent`:

1. Extract upstream `Java/model.mustache` using the consumer-selected generator version
2. Apply deterministic patch
3. Overlay platform templates
4. Execute the official Maven plugin with the aligned generator dependencies and selected `java-generics-contract` specialization
5. Register generated sources
6. Normalize generated Java through the configured unused-import removal and formatting lifecycle

Maven template lifecycle opt-out:

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

This skips the generics-aware extraction, patching, overlay, and normalization lifecycle. It does not change `generatorName`; returning to stock reconstruction behavior also requires selecting the upstream `java` generator. Do not assume this Maven property is a Gradle opt-out API.

### Gradle Integration

Implemented in `openapi-generics-gradle-plugin`:

1. Use `withPlugin("org.openapi.generator", ...)` to integrate after the official plugin is present; do not apply or replace it implicitly.
2. Validate `openApiGeneratorExtra` and provision the platform-version-aligned custom codegen artifact.
3. Discover the upstream template artifact from the running official plugin classloader; missing or ambiguous template ownership must fail.
4. Prepare a shared effective template directory through upstream extraction, fail-fast dispatch patching, and platform overlay.
5. Lazily select official `GenerateTask` instances with `generatorName=java-generics-contract`, including custom/multiple tasks; wire prepared templates, preparation dependencies, and generator classpath to each selected task.
6. When the Java plugin is present, register selected generated Java roots and compilation dependencies using each task's output directory and `sourceFolder`.
7. Normalize selected generated Java with the embedded formatter runtime as the generation task's final action, before output snapshots are taken.

Preserve non-selected generator tasks: do not impose generics templates, generation dependencies, source registration, or normalization on them, and preserve consumer-owned ordinary options and template settings.

Keep providers lazy and configuration-cache safe. Install project-level Java/source-set wiring outside `GenerateTask.configureEach` callbacks to respect Gradle 9.x mutation/lifecycle constraints. Capture normalization state during configuration; do not look up project state or task extensions during its execution. Consumers do not need to apply the Spotless Gradle plugin for generated-source normalization.

---

# Generic Container Infrastructure

OpenAPI Generics 1.1.x introduces first-class container support.

Container handling is a platform capability, not a Page-specific implementation.

Supported containers:

```java
List<T>
Set<T>
Page<T>
```

Explicitly registered application-defined generic containers also participate in this infrastructure. Configuration validates a concrete class or record with one type parameter and a declared item property of `List<T>` or `Set<T>` using that parameter directly. The shared container type must be available to the generated client; arbitrary generic graphs and unregistered container inference remain unsupported.

Supported examples:

```java
ServiceResponse<T>

ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>

ApiResponse<T>

ApiResponse<List<T>>
ApiResponse<Set<T>>
ApiResponse<Page<T>>
```

Container processing consists of:

1. Container Detection
2. Generic Item Resolution
3. Metadata Construction
4. OpenAPI Enrichment
5. Wrapper Reconstruction
6. Generated Type Binding

Container semantics must be represented through metadata rather than hardcoded branching.

---

# Vendor Extension Protocol

These extensions carry contract semantics through the OpenAPI document.

| Extension | Direction | Purpose |
|------------|------------|------------|
| `x-api-wrapper` | server → client | Boolean wrapper marker |
| `x-api-wrapper-type` | server → client | Fully qualified Java envelope identity |
| `x-api-wrapper-datatype` | server → client | Payload type |
| `x-data-container` | server → client | Semantic/simple container name used in reconstruction |
| `x-data-container-type` | server → client | Fully qualified Java container identity/import |
| `x-data-item` | server → client | Item type |
| `x-ignore-model` | server → client | Prevent duplicate model generation while retaining the OpenAPI schema |

Codegen-internal extensions are derived during reconstruction, not additional producer protocol requirements:

| Extension | Purpose |
|-----------|---------|
| `x-envelope-import` | Envelope import derived from `x-api-wrapper-type` |
| `x-envelope-type` | Envelope simple name derived from `x-api-wrapper-type` |
| `x-extra-imports` | External payload imports derived from BYOC mappings |

`x-payload-property` is not an active canonical protocol extension. The server carries the payload property in its response descriptor for schema resolution and validation. Validate wrapper metadata before reconstruction; container name, Java type, and item metadata must be supplied together.

---

# Coding Patterns

## Controllers

Preferred shapes:

```java
ResponseEntity<ServiceResponse<T>>

ResponseEntity<ServiceResponse<List<T>>>

ResponseEntity<ServiceResponse<Set<T>>>

ResponseEntity<ServiceResponse<Page<T>>>
```

Equivalent BYOE shapes are supported.

---

## Generated Wrappers

Generated wrappers extend shared contract types.

They bind concrete response schemas to generic Java types for transport/deserialization. Application contracts remain the shared Java types, not generated wrapper classes.

Examples:

```java
public class ServiceResponseCustomerDto
    extends ServiceResponse<CustomerDto> {}
```

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

```java
public class ServiceResponseListCustomerDto
    extends ServiceResponse<List<CustomerDto>> {}
```

```java
public class ServiceResponseSetCustomerDto
    extends ServiceResponse<Set<CustomerDto>> {}
```

---

## BYOC Mapping

Pass the mapping through generator `additionalProperties` (Maven example):

```xml
<additionalProperties>
    <additionalProperty>openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto</additionalProperty>
</additionalProperties>
```

---

## BYOE Configuration

Configure the envelope on the producer:

```yaml
openapi-generics:
  envelope:
    type: com.example.ApiResponse
```

The producer publishes that identity through `x-api-wrapper-type`. The client derives its envelope import/type from this metadata; aligned producer/codegen components do not require redundant client-side envelope configuration. The application-owned envelope must be available on the generated client's compile classpath.

---

# Design Constraints

## Supported Envelope Shape

The envelope must expose a single direct payload slot.

BYOE validation requires a concrete class with exactly one generic parameter and one direct payload field of that parameter's type. Record envelopes and nested generic payload slots are unsupported; a container payload is supplied as the envelope's type argument at the usage site.

Supported:

```java
ApiResponse<T>

ApiResponse<List<T>>

ApiResponse<Set<T>>

ApiResponse<Page<T>>
```

Unsupported:

```java
ApiResponse<T, M>
```

```java
ApiResponse<T, E>
```

Multiple generic parameters are not supported.

## Supported Runtime Scope

* Spring MVC
* Synchronous controllers
* Supported async wrappers

## Unsupported Scope

* WebFlux
* Cross-language reconstruction
* Arbitrary generic graph reconstruction

---

# Deterministic Validation

OpenAPI specifications are treated as validation artifacts.

The platform validates:

* projection correctness
* vendor extension consistency
* wrapper reconstruction
* generated client compatibility
* specification drift

Sample projects include committed OpenAPI snapshots.

Specification drift must be visible during CI execution.

---

# Build Commands

```bash
mvn clean install

mvn -f samples/pom.xml clean verify

mvn -pl openapi-generics-server-starter test

mvn clean deploy -P central-publish

mvn spotless:apply
```

Gradle plugin verification (run from `openapi-generics-gradle-plugin/`):

```bash
./gradlew test

./gradlew functionalTest

./gradlew check

./gradlew e2eTest
```

`check` includes unit tests and functional TestKit tests; `e2eTest` is explicit and builds the real Maven codegen artifact into an isolated repository. Functional tests use a fixture codegen artifact and do not replace real generation/compilation verification. The Gradle build is not invoked by the root Maven reactor.

The maintained Gradle adoption sample has its own wrapper under `samples/gradle/customer-service-client/`; after installing its required platform/shared-contract snapshot artifacts, run `./gradlew clean build --configuration-cache` there. Maven samples require the appropriate JDK, including Java 25 for the Boot 4 reference stack.

---

# POM Hygiene Rules

* Use `openapi-generics-platform-bom` for its internal platform dependency alignment scope; do not turn it into a consumer runtime or generator version authority.
* Avoid duplicate version declarations.
* Keep consumer POMs minimal.
* Use Spotless for formatting.
* Prefer platform-managed defaults within their ownership scope while preserving consumer version selection and selected toolchain alignment.

---

# Testing Philosophy

* Unit tests validate deterministic behavior.
* Samples provide end-to-end verification.
* Contract regressions must be visible.
* Specification drift must fail validation.
* Template drift must fail the build.
* Invalid platform-owned invariants must fail explicitly; preserve intentional non-error outcomes.

---

# Decision Framework

When evaluating any change, prioritize:

1. Contract integrity
2. Architectural correctness
3. Deterministic behavior
4. Upgrade stability
5. Developer experience
6. OSS maintainability
7. Adoption simplicity

---

# Output Quality Standard

Every recommendation should:

* Respect module boundaries
* Explain trade-offs
* Prefer the simplest correct solution
* Avoid speculative abstractions
* Preserve long-term maintainability

---

# Summary

`openapi-generics` is a contract-first platform where:

* Java is the authoritative contract.
* OpenAPI is a projection.
* Generic containers are first-class platform concepts.
* Shared models are reused rather than regenerated.
* OpenAPI specifications are validation artifacts.
* Build and runtime behavior are deterministic.
* Misconfigurations fail fast.
* Developer adoption is a primary design goal.
