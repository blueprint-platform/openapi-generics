# type-coverage

Focused end-to-end validation samples for OpenAPI Generics.

Unlike the Spring Boot integration samples, the projects in this directory are not intended to demonstrate application architecture, business workflows, or framework usage patterns.

Their purpose is to provide deterministic validation environments for verifying the core OpenAPI Generics pipeline and preventing regressions across future releases.

---

# Purpose

Each sample isolates a specific contract model and validates the complete lifecycle:

```text
Contract
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Runtime Deserialization
    ↓
Consumer Usage
```

The focus is not endpoint functionality.

The focus is preserving type fidelity across the entire projection and reconstruction process.

---

# What Is Validated

The samples collectively verify:

- OpenAPI projection correctness
- Generic wrapper reconstruction
- Generated client type safety
- Runtime deserialization behavior
- Vendor extension processing
- External model handling
- Envelope reconstruction
- Collection reconstruction
- Page reconstruction
- Consumer compatibility
- Backward compatibility
- Regression protection

---

# Design Principles

Each sample follows the same structure:

```text
Producer
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Consumer
```

The consumer uses only generated artifacts.

No manual DTO reconstruction exists.

No custom deserializers exist.

No adapter-level type conversion exists.

The generated client must reconstruct the original contract shape automatically.

---

# Available Samples

| Sample | Purpose |
|----------|----------|
| `service-response` | Validates the canonical `ServiceResponse<T>` contract, including scalar, object, collection, and paged payload reconstruction |
| `byoe-response` | Validates Bring Your Own Envelope (BYOE) support using a completely user-owned `ApiResponse<T>` contract |

---

# ServiceResponse Coverage

Validates the platform-provided envelope:

```java
ServiceResponse<T>
```

Covered scenarios include:

```java
ServiceResponse<String>

ServiceResponse<UUID>

ServiceResponse<AddressDto>

ServiceResponse<List<TypeSummaryDto>>

ServiceResponse<Set<TypeSummaryDto>>

ServiceResponse<Page<TypeSummaryDto>>
```

The purpose is to verify the baseline projection and reconstruction behavior of the canonical OpenAPI Generics contract.

---

# BYOE Coverage

Validates a user-owned envelope:

```java
ApiResponse<T>
```

Covered scenarios include:

```java
ApiResponse<String>

ApiResponse<UUID>

ApiResponse<AddressDto>

ApiResponse<List<TypeSummaryDto>>

ApiResponse<Set<TypeSummaryDto>>

ApiResponse<Page<TypeSummaryDto>>
```

The purpose is to verify that OpenAPI Generics can reconstruct generic contracts that are not owned by the platform.

No changes to the original response model are required.

---

# Regression Strategy

These samples function as executable regression suites.

Their responsibility is to detect failures in:

- projection generation
- vendor extension processing
- ignored model handling
- external model resolution
- generic wrapper reconstruction
- collection reconstruction
- page reconstruction
- generated client typing
- runtime deserialization

before such failures reach real integration projects.

---

# Mental Model

OpenAPI Generics ultimately guarantees preservation of contract intent.

```text
Original Contract
         ↓
OpenAPI Projection
         ↓
Generated Sources
         ↓
Runtime Reconstruction
         ↓
Consumer Contract
```

A successful type-coverage sample proves that the consumer receives the same generic contract shape that originally existed in the producer.

This repository exists to continuously verify that guarantee.