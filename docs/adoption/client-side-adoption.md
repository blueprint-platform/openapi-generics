---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
has_toc: false
---

# Client-Side Adoption — Generate a Contract-Aligned Java Client

> Generate a Java client that **preserves contract semantics exactly as published** — with progressive adoption, zero duplication, and no drift.

This is **not a typical OpenAPI client setup**.

It is a controlled, opt-in build-time pipeline where:

- OpenAPI is treated as **input metadata**, not authority
- the contract is **preserved**, not regenerated
- the output is **deterministic** when enabled
- the system is **selectively bypassable** when you need stock behavior

This guide focuses on four things:

- consuming OpenAPI as input
- executing the contract-aware build pipeline
- aligning the generated client with an external contract (BYOE / BYOC)
- using the generated client safely from your application

For server-side projection mechanics, see [Server-Side Adoption](./server-side-adoption.md).
For internal pipeline mechanics, see [Architecture](../architecture/architecture.md).

---

## Contents

- [60-second quick start](#60-second-quick-start)
- [What the client actually does](#what-the-client-actually-does)
- [Input: OpenAPI (not your contract)](#input-openapi-not-your-contract)
- [Minimal setup](#minimal-setup)
- [Progressive adoption modes](#progressive-adoption-modes)
- [Build pipeline (what really happens)](#build-pipeline-what-really-happens)
- [Output: what gets generated](#output-what-gets-generated)
- [Usage: how the client enters your system](#usage-how-the-client-enters-your-system)
- [Common adoption pitfalls](#common-adoption-pitfalls)
- [Verification](#verification)
- [Error handling](#error-handling)

---

## 60-second quick start

You want:

- a type-safe client
- zero duplicated envelope models
- preserved generic response semantics across server, spec, and client

Do this:

### 1) Inherit the parent

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.0.1</version>
</parent>
```

### 2) Provide an OpenAPI document

Either a static file checked into the client module:

```text
src/main/resources/your-api-docs.yaml
```

Or fetched from a running producer:

```text
http://localhost:8084/customer-service/v3/api-docs.yaml
```

### 3) Configure the generator (minimal)

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>generate-client</id>
      <phase>generate-sources</phase>
      <goals><goal>generate</goal></goals>
      <configuration>
        <generatorName>java-generics-contract</generatorName>
        <inputSpec>${project.basedir}/src/main/resources/your-api-docs.yaml</inputSpec>
        <library>restclient</library>

        <apiPackage>com.example.client.api</apiPackage>
        <modelPackage>com.example.client.dto</modelPackage>
        <invokerPackage>com.example.client.invoker</invokerPackage>

        <configOptions>
          <useSpringBoot3>true</useSpringBoot3>
          <serializationLibrary>jackson</serializationLibrary>
          <openApiNullable>false</openApiNullable>
        </configOptions>
      </configuration>
    </execution>
  </executions>
</plugin>
```

### 4) (Optional) Align with your own contract

Reuse your DTOs (BYOC):

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Add one `openapi-generics.response-contract.<OpenAPI model name>` property per externally provided DTO you want the generated client to reuse.

Use your own envelope (BYOE):

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=com.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

### 5) Build

```bash
mvn clean install
```

That's it.

---

### Result

Default envelope:

```java
public class ServiceResponseCustomerDto
    extends ServiceResponse<CustomerDto> {}

public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

Custom envelope (BYOE):

```java
public class ApiResponseCustomerDto
    extends ApiResponse<CustomerDto> {}
```

One envelope. Generics preserved. Same contract on the server, in the OpenAPI document, and in every generated wrapper.

> Working references: [`samples/spring-boot-3/customer-service-client`](https://github.com/blueprint-platform/openapi-generics/tree/main/samples/spring-boot-3/customer-service-client) (BYOC enabled) and [`samples/spring-boot-4/customer-service-client`](https://github.com/blueprint-platform/openapi-generics/tree/main/samples/spring-boot-4/customer-service-client) (zero-configuration default flow). The two samples differ on purpose — they show that BYOE and BYOC are alignment inputs, not requirements.

---

## What the client actually does

The client has **one responsibility**:

> Convert an OpenAPI document into a contract-aligned Java client without redefining anything.

It does **not**:

- design models
- interpret business semantics
- introduce abstractions over your domain

It executes a deterministic transformation:

```text
OpenAPI document → contract-aligned Java client
```

Same input, same output, every build.

---

## Input: OpenAPI (not your contract)

Client generation always starts from an existing OpenAPI document.

```bash
curl http://localhost:8084/customer-service/v3/api-docs.yaml -o customer-api-docs.yaml
```

The critical distinction:

> OpenAPI is **input metadata**, not the contract itself.

What this means in practice:

- **Structure** comes from OpenAPI — paths, operations, response shapes.
- **Semantics** come from contract types — your envelope and DTOs, either generated by default or sourced from your classpath via BYOE/BYOC.

The system never trusts OpenAPI to be the source of truth for type identity. It uses the document's vendor extensions (`x-api-wrapper`, `x-data-container`, `x-data-item`, `x-ignore-model`) to reconstruct the original generic shape.

### Spec freshness is part of the contract

A spec checked into source control is a snapshot. The platform cannot detect when the producer has moved on; if your build still passes against an old spec, the client is old too. Decide on a refresh cadence before this becomes a debugging story:

- **Manual** — fetch and commit the spec when the producer team announces a contract change.
- **CI-driven** — a scheduled job that re-fetches the spec, regenerates the client, and opens a PR if anything changed.
- **Build-time fetch** — pull the spec directly from a running producer during the build (works for local development, less so for offline CI).

The right choice depends on your release cadence and how tightly the producer and consumer are coupled. The platform takes no opinion — it just guarantees that whatever spec you feed it is reconstructed deterministically.

---

## Minimal setup

You provide exactly two inputs. Everything else is handled by the platform.

### 1. Build-time orchestration (mandatory)

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.0.1</version>
</parent>
```

This is the **entry point** of the system. It provides:

- generator binding (`java-generics-contract`)
- the extract → patch → overlay template pipeline
- deterministic execution model
- automatic registration of generated sources for compilation

You do not need to manage any of these concerns yourself.

### 2. OpenAPI Generator plugin (your input surface)

You control the **input and integration surface only**. At minimum:

- the OpenAPI input (`inputSpec`)
- the generator (`java-generics-contract`)
- the HTTP client library (`library`)
- the Java package layout (`apiPackage`, `modelPackage`, `invokerPackage`)

Everything else (template chain, contract behavior, wrapper logic) is handled by the parent.

### Full reference configuration

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <id>generate-client</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>generate</goal>
      </goals>

      <configuration>

        <generatorName>java-generics-contract</generatorName>

        <inputSpec>${project.basedir}/src/main/resources/your-api-docs.yaml</inputSpec>

        <library>restclient</library>

        <apiPackage>com.example.client.api</apiPackage>
        <modelPackage>com.example.client.dto</modelPackage>
        <invokerPackage>com.example.client.invoker</invokerPackage>

        <configOptions>
          <!-- Choose ONE depending on your runtime -->
          <!-- Spring Boot 3 -->
          <useSpringBoot3>true</useSpringBoot3>

          <!-- Spring Boot 4 -->
          <!-- <useSpringBoot4>true</useSpringBoot4> -->

          <serializationLibrary>jackson</serializationLibrary>
          <openApiNullable>false</openApiNullable>
        </configOptions>

        <!-- Optional: Bring Your Own Contract (external DTOs) -->
        <!-- Add one property per externally provided OpenAPI model. -->
        <!--
        <additionalProperties>
            <additionalProperty>
              openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto
            </additionalProperty>
            <additionalProperty>
              openapi-generics.response-contract.AddressDto=com.example.contract.AddressDto
            </additionalProperty>
        </additionalProperties>
        -->

        <!-- Optional: Bring Your Own Envelope (BYOE) -->
        <!--
        <additionalProperties>
          <additionalProperty>
            openapi-generics.envelope=com.example.contract.ApiResponse
          </additionalProperty>
        </additionalProperties>
        -->

        <cleanupOutput>true</cleanupOutput>
        <skipValidateSpec>false</skipValidateSpec>

        <generateApiDocumentation>false</generateApiDocumentation>
        <generateApiTests>false</generateApiTests>
        <generateModelDocumentation>false</generateModelDocumentation>
        <generateModelTests>false</generateModelTests>

      </configuration>

    </execution>
  </executions>
</plugin>
```

### What you control

- which OpenAPI spec is used
- which HTTP client is generated (`library`)
- package structure
- serialization strategy
- optional external contract mappings (BYOC)
- optional envelope override (BYOE)

### What the platform owns

- generator internals
- template extraction, patching, and overlay
- wrapper generation rules
- vendor extension handling
- contract integrity checks performed at generation time

> The generator is **contract-driven**, not schema-driven.

### Reference implementations

For complete, working setups:

```text
samples/
  spring-boot-3/customer-service-client    ← BYOC enabled
  spring-boot-4/customer-service-client    ← zero-configuration default flow
```

Both demonstrate:

- the minimal POM shape
- how the parent POM orchestration is inherited
- how generated sources are consumed downstream

The Spring Boot 3 sample additionally demonstrates BYOC against a shared `customer-contract` module; the Spring Boot 4 sample runs without BYOC or BYOE so you can see the default flow on its own.

---

## Progressive adoption modes

The system separates two concerns that are often conflated:

- **Modes** control **how the generator behaves**.
- **Alignment inputs** control **how the contract is resolved**.

These are orthogonal. You can change one without affecting the other.

### 1. Build-time modes (execution behavior)

#### 1.1 Contract-aligned mode (default)

```xml
<openapi.generics.skip>false</openapi.generics.skip>
```

Behavior:

- contract-aware generation is enabled
- wrapper classes are generated as **type bindings** (thin `extends` declarations)
- generic structure is preserved
- OpenAPI is **interpreted**, not materialized class-for-class

This is the standard operating mode.

#### 1.2 Compatibility mode (fallback)

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

Behavior:

- the contract-aware build pipeline is bypassed entirely
- the underlying `openapi-generator-maven-plugin` runs with stock templates
- models are generated directly from schemas
- generics may be flattened, envelopes may be duplicated per endpoint

Use this when:

- comparing outputs side-by-side
- isolating whether an issue originates upstream or in the contract layer
- migrating an existing client incrementally

| `openapi.generics.skip` | Behavior                  |
| ----------------------- | ------------------------- |
| `false` (default)       | Contract-aware generation |
| `true`                  | Standard OpenAPI Generator |

This single switch is the clean exit path. If you need stock generator behavior, flip `openapi.generics.skip` to `true` — the generator name stays as `java-generics-contract`, the parent stays inherited, and the pipeline simply skips its contract-aware steps. There's no fork to unwind.

### 2. Contract alignment inputs (orthogonal to modes)

These do **not** change the execution mode. They tell the generator how to resolve the contract during generation.

#### 2.1 BYOE — Bring Your Own Envelope

Define which envelope type represents your contract.

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=com.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

Behavior:

- the generator resolves your envelope type explicitly
- generated wrappers extend your envelope
- no envelope class is generated client-side
- the system becomes **envelope-agnostic** — the same pipeline serves the default and BYOE cases

Example output:

```java
public class ApiResponseCustomerDto
    extends ApiResponse<CustomerDto> {}
```

Constraints (validated at server startup; see [server-side guide](./server-side-adoption.md#byoe--envelope-shape-requirements) for details):

- must be a concrete class
- must declare exactly one type parameter
- must expose a single direct payload field of type `T`

> **Scope:** BYOE supports envelopes with a single direct generic payload (`YourEnvelope<T>`). Nested forms like `YourEnvelope<Page<T>>` are out of scope and fail fast at startup.

#### 2.2 BYOC — Bring Your Own Contract

Reuse externally owned DTOs instead of regenerating them.

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Behavior:

- DTOs are resolved from your classpath, not generated
- no duplicate model is produced
- generated wrappers import your existing types directly

Example output:

```java
public class ServiceResponseCustomerDto
    extends ServiceResponse<CustomerDto> {}
```

The `CustomerDto` referenced here is **your** `com.example.contract.CustomerDto` — not a regenerated copy.

BYOC applies to payload types used inside generated wrappers, including nested
generic structures such as `ServiceResponse<Page<CustomerDto>>`.

### 3. How they compose

Modes and alignment inputs are independent. A typical production setup uses both:

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=com.example.contract.ApiResponse
  </additionalProperty>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Result:

```text
Envelope  → external (BYOE)
DTO       → external (BYOC)
Wrappers  → generated (thin bindings)
```

### 4. Summary

```text
Mode controls execution    →  openapi.generics.skip = false / true
Inputs control alignment   →  openapi-generics.envelope / response-contract.*
```

The system works because:

- execution is explicitly controlled
- contract ownership is externalizable
- generation remains deterministic
- adoption is fully reversible

> The generator does not invent models. It resolves which envelope to use (BYOE), which DTOs to reuse (BYOC), and how to bind them deterministically.

---

## Build pipeline (what really happens)

The parent POM orchestrates a five-stage pipeline during `generate-sources`. You do not invoke these stages yourself — they execute automatically when you inherit the parent.

```text
OpenAPI spec (input)
   │
   ▼
1. Extract upstream model.mustache from openapi-generator JAR
   │      → maven-dependency-plugin
   │      → output: target/effective-templates/Java/model.mustache
   ▼
2. Patch the upstream template with surgical regex insertions
   │      → maven-antrun-plugin
   │      → injects two replacements into the {{#models}}{{#model}} loop
   │      → fails fast if upstream structure changed
   ▼
3. Extract platform templates from openapi-generics-java-codegen JAR
   │      → maven-dependency-plugin
   │      → output: target/codegen-templates/META-INF/openapi-generics/templates/
   ▼
4. Overlay platform templates onto the patched upstream
   │      → maven-resources-plugin
   │      → adds api_wrapper.mustache (the partial referenced by the patch)
   ▼
5. Run openapi-generator-maven-plugin
          → templateDirectory points at target/effective-templates/Java
          → generatorName = java-generics-contract → GenericAwareJavaCodegen
          ▼
   Generated sources (contract-aligned)
```

The build-helper plugin then registers the generated sources for compilation. The whole pipeline is governed by a single switch:

```xml
<openapi.generics.skip>false</openapi.generics.skip>
```

When `true`, every contract-aware step is skipped and the build falls back to stock OpenAPI Generator behavior.

### Why patch-then-overlay instead of just overlay?

A drop-in template directory is the obvious approach — but it freezes a snapshot of upstream `model.mustache` at the moment you copied it. As OpenAPI Generator evolves, your snapshot drifts further behind, and behavior changes silently.

The patch approach inverts this: upstream stays as the source of structure, and the platform injects **only** the generic-aware branch. If upstream restructures the `{{#models}}{{#model}}` loop in a way that the regex no longer matches, the build fails immediately:

```
OpenAPI template patch FAILED — upstream model.mustache structure changed.
```

The trade-off: occasional upstream-bump pain during major version updates, in exchange for durability against silent drift.

### What changes vs standard OpenAPI generation

| Concern | Stock OpenAPI Generator | openapi-generics |
| --- | --- | --- |
| Envelope schema | Materialized as a class per endpoint (`ServiceResponseCustomerDto`, `ServiceResponseOrderDto`, …) | Single shared envelope; wrappers are thin `extends` bindings |
| Generics | Flattened in generated wrappers | Preserved (`extends ServiceResponse<Page<CustomerDto>>`) |
| External DTOs | Regenerated from schema | Reused from classpath when BYOC is configured |
| Generated output | Schema-driven materialization | Contract-driven binding |

### Pipeline guarantees

- single orchestrator (no plugin ordering issues)
- fixed execution flow (no runtime branching)
- deterministic output (same spec + same configuration → same generated code, byte-for-byte stable across builds)
- contract-driven, not schema-driven
- envelope-agnostic (default and BYOE share the same pipeline)

---

## Output: what gets generated

The system does not generate envelope or contract DTOs in the traditional sense. It generates **thin, contract-aligned wrapper types** that bind OpenAPI responses back to your canonical contract.

### From the OpenAPI document

The producer publishes wrapper schemas like:

```text
ServiceResponseCustomerDto
ServiceResponsePageCustomerDto
```

### Generated Java

Default envelope, single payload:

```java
public class ServiceResponseCustomerDto
    extends ServiceResponse<CustomerDto> {}
```

Default envelope, paginated payload:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

Custom envelope (BYOE):

```java
public class ApiResponseCustomerDto
    extends ApiResponse<CustomerDto> {}
```

That is the entire body. The wrapper exists to give Jackson a concrete type to bind into; the structure lives entirely in the envelope and contract types it extends.

### What is NOT generated

When the contract-aware pipeline is active:

- `ServiceResponse` (or your BYOE envelope) — imported from the contract module
- `Meta`, `Sort` — imported from the contract module
- `Page` — imported from the contract module
- DTOs mapped via BYOC — imported from your existing modules

This is the structural reason the generated `dto/` package looks small: it contains wrappers, not contract material.

### Deterministic naming

Wrapper class names are derived from:

```text
envelope simple name + container name (if any) + item simple name
```

Examples:

```text
ServiceResponseCustomerDto
ServiceResponsePageCustomerDto
ApiResponseCustomerDto
ApiResponsePageCustomerDto
```

Same envelope + same payload + same supported container → same generated class name. Diffs are stable across builds.

---

## Usage: how the client enters your system

Generated sources are added to your project automatically:

```text
target/generated-sources/openapi/src/gen/java
```

These sources are **not your domain layer**. They are an **integration boundary**.

### What you actually use

Application code depends on the contract shape, not the generated wrapper:

```java
ServiceResponse<CustomerDto>
```

Or, with BYOE:

```java
ApiResponse<CustomerDto>
```

This is the only type your business logic needs to reference. Generated wrapper classes (`ServiceResponseCustomerDto`, `ServiceResponsePageCustomerDto`, …) exist for transport — Jackson binds incoming JSON into them, and they extend the contract so your code reads them as the contract type.

### How the client enters your system

Generated APIs are consumed through a controlled adapter boundary:

```java
public interface CustomerClient {

  ServiceResponse<CustomerDto> getCustomer(Long id);

}
```

Implementation delegates to the generated client:

```java
@Service
public class CustomerClientImpl implements CustomerClient {

  private final CustomerControllerApi api;

  public CustomerClientImpl(CustomerControllerApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Long id) {
    return api.getCustomer(id);
  }
}
```

### Why this boundary matters

Generated code is a **transport concern**, not a domain concern. The adapter is where you translate between them.

Without this boundary, generated types leak into your application — binding your domain to OpenAPI output, generator conventions, and the transport itself.

With the adapter in place:

- **Domain purity** — your application depends on contract types, not generated artifacts
- **Testability** — business logic is tested against the contract, not the HTTP client
- **Contract stability** — generator updates do not ripple into domain code
- **Replaceability** — the underlying transport can change without touching the application

The adapter is not boilerplate. It is the seam between *what your system means* and *how it talks to the outside*.

### Data flow through your system

```text
Controller → Contract → Adapter → Generated Client → HTTP
```

### Reference implementations

```text
samples/
  spring-boot-3/customer-service-consumer
  spring-boot-4/customer-service-consumer
```

These demonstrate adapter-based integration, contract-first usage, and safe isolation of generated code.

---

## Common adoption pitfalls

These aren't system failures — they're shapes that look right but produce surprising results during early adoption. The platform's deterministic and fail-fast properties catch the most consequential issues at build time; the notes here help you recognize the rest sooner.

### Generated wrapper types in domain code

Generated wrappers (`ServiceResponseCustomerDto`, `ServiceResponsePageCustomerDto`, …) are deserialization targets. They exist so Jackson has a concrete class to bind into — not to be referenced from business logic.

If you find yourself writing:

```java
ServiceResponseCustomerDto response = api.getCustomer(id);
```

…in domain code, the adapter boundary is missing. The fix is structural: introduce an adapter that returns `ServiceResponse<CustomerDto>` (the contract type) and keep the wrapper class confined to the adapter implementation. See [Usage — how the client enters your system](#usage-how-the-client-enters-your-system).

### Switching to stock generator behavior

If you want plain OpenAPI Generator output for a module — for comparison, debugging, or incremental migration — the clean path is the skip switch:

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

Keep the generator name (`java-generics-contract`) and the inherited parent. The pipeline detects the flag, bypasses its contract-aware steps, and the build runs against stock templates.

Changing the generator name to `java` while keeping the parent's template overrides puts the build in an inconsistent state — patched templates pointed at by `<templateDirectory>` fed to a generator that doesn't expect them. The skip switch avoids this entirely.

### Custom templates in `templateDirectory`

The parent POM sets `<templateDirectory>` to the patched-and-overlaid output of the build pipeline. Pointing it elsewhere — for example, at a hand-curated `src/main/resources/openapi-templates` folder — disables the patch-then-overlay flow and replaces it with a frozen snapshot that drifts from upstream silently.

If you need a behavioral change in template output, the durable path is contributing the change upstream (to OpenAPI Generator itself or to `openapi-generics-java-codegen`'s overlay templates) rather than holding a local fork. Local template overrides accumulate technical debt against future generator upgrades.

### BYOC mappings without the matching classpath dependency

A BYOC mapping like:

```xml
<additionalProperty>
  openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto
</additionalProperty>
```

…tells the generator to emit `import com.example.contract.CustomerDto;` instead of generating a local copy. For this to compile, `com.example.contract.CustomerDto` must be on the client module's classpath — typically via a dependency on a shared `*-contract` module.

If the dependency isn't there, the generated client builds wrappers like `extends ServiceResponse<CustomerDto>` against a class the compiler can't find, and the build fails with `cannot find symbol`. The fix is to add the contract module as a dependency, not to remove the BYOC mapping.

### Out-of-date OpenAPI spec

A spec checked into source control doesn't refresh itself. If the producer team adds endpoints, changes wrapper shapes, or updates an envelope, your client still sees the old contract until you re-fetch the spec. The build will pass; the runtime will fail or, worse, silently miss new fields.

Pick a refresh strategy at adoption time — manual on producer announcements, scheduled CI fetch, or build-time fetch from a running producer — and document it alongside the client module. See [Spec freshness is part of the contract](#spec-freshness-is-part-of-the-contract).

---

## Verification

After generation, verify the following:

### 1. Wrapper classes extend the correct contract type

Open `target/generated-sources/openapi/src/gen/java/.../dto/`. You should see thin classes like:

```java
public class ServiceResponseCustomerDto
    extends ServiceResponse<CustomerDto> {}

public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

If wrappers are full classes with `data`, `meta`, getters, setters, and `@JsonProperty` annotations — the contract-aware pipeline did not run. Check that:

- `<generatorName>` is `java-generics-contract` (not `java`)
- the parent POM is correctly inherited
- `openapi.generics.skip` is not set to `true`

### 2. No envelope or contract-infrastructure classes are regenerated

The generated `dto/` package should **not** contain:

- `ServiceResponse` (or your BYOE envelope)
- `Meta`, `Sort`
- `Page`
- any DTO you mapped via BYOC

If any of these appear as standalone generated classes, the server-side projection did not stamp them as `x-ignore-model: true`. See [server-side verification](./server-side-adoption.md#verification).

### 3. Generics are preserved

```java
extends ServiceResponse<Page<CustomerDto>>
```

If you see flattened forms like `extends ServiceResponsePageCustomerDto` (with no generics) or duplicated envelope structures per endpoint — the build is in compatibility mode, not contract-aligned mode.

### 4. The client compiles against your contract

A successful `mvn clean install` confirms that:

- the generated wrappers reference types resolvable on the classpath
- BYOC mappings resolved to real classes (no `cannot find symbol` errors)
- BYOE envelope, if configured, is on the classpath as a transitive dependency

If verification passes:

```text
OpenAPI → Client → Contract alignment is correct.
```

---

## Error handling

Error handling is **not enforced by the client generator**. It depends on the contract pattern your service publishes.

Two common patterns:

### 1. Separate error protocol (recommended for new services)

```text
Success → ServiceResponse<T>
Error   → ProblemDetail (RFC 9457)
```

The generated client deserializes successes into the wrapper type and errors into `ProblemDetail`. Your adapter layer decides how to surface them.

### 2. Envelope-based error model

```text
Success → YourEnvelope<T>
Error   → YourEnvelope<T> (errors carried inside the envelope)
```

The generated client deserializes both branches into the same wrapper type. Your adapter inspects the envelope and routes accordingly.

### Key point

```text
The generator does not define error semantics.
It preserves whatever contract the service exposes.
```

Choosing between the two patterns is a service-level decision, made on the producer side. See [server-side error responses](./server-side-adoption.md#error-responses--pick-a-pattern-stay-consistent) for the trade-offs.

---

## Further reading

- [Server-Side Adoption](./server-side-adoption.md) — what changes in your producer service
- [Architecture](../architecture/architecture.md) — internal pipeline, vendor extension protocol, design decisions
- [Compatibility & Support Policy](../compatibility.md) — supported version matrix
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions) — design questions, edge cases, OAS 3.1 compliance