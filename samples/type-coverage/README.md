# type-coverage

Focused validation samples for OpenAPI Generics.

Unlike the Spring Boot integration samples, the projects in this directory are not intended to demonstrate application architecture or business workflows.

Their purpose is to provide deterministic environments for validating:

- OpenAPI projection
- generic wrapper reconstruction
- generated client behavior
- vendor extension processing
- contract compatibility
- regression scenarios

Each sample isolates a specific contract shape and exercises it end-to-end through the complete OpenAPI Generics pipeline:

```text
Contract
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Consumer Deserialization
```

## Available samples

| Sample | Purpose |
|----------|----------|
| `service-response` | Validates the canonical `ServiceResponse<T>` contract and nested generic scenarios such as `ServiceResponse<Page<T>>` |
| `byoe-response` | Validates Bring Your Own Envelope (BYOE) support using a custom user-owned `ApiResponse<T>` contract |

See the individual sample documentation for implementation details and supported scenarios.