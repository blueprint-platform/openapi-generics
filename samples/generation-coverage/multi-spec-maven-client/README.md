# Two specifications, one Maven client artifact

`MultiSpecGenerationProbe` references generated API/wrapper types from two executions. If an execution
is missing, changes package, or loses output during cleanup, normal Java compilation fails. This is
build-time evidence, not runtime HTTP interoperability or an application boundary design.

## Execution map

| Execution | Existing input | Output under `target/generated-sources` | Package suffix |
| --- | --- | --- | --- |
| `generate-customer-client` | `../../spring-boot-3/spec/customer-api-docs.yaml` | `customer/src/gen/java` | `generationcoverage.customer.*` |
| `generate-service-response-client` | `../../type-coverage/service-response/spec/service-response-coverage.yaml` | `service-response/src/gen/java` | `generationcoverage.serviceresponse.*` |

Each execution owns output, API/model/invoker packages and cleanup. The customer execution maps
`CustomerDto` to the application-owned shared contract through BYOC (Bring Your Own Contract); that mapping does not leak into
the service-response execution. Both use the same shared reconstruction semantics.

The compile probe intentionally references generated classes to test their presence and type bindings.
It is not a public application API to copy; expose an adapter in an actual client library.

## Run and inspect

After [platform setup](../../README.md#repository-build-and-local-runtime-setup), from repository root:

```bash
mvn -f samples/pom.xml -pl generation-coverage/multi-spec-maven-client -am clean verify
```

Inspect the two output roots and `src/main/java/.../MultiSpecGenerationProbe.java`. Representative
bindings include direct/paged customer and scalar/list/page/application-container responses. These are
transport subclasses around shared generic contracts, not replacement contract definitions.

Compilation verifies source registration and isolation. It does not establish that the Maven parent's
default normalization include covers every custom output root; source registration and normalization
are separate checks. This module has no Boot runtime, transport customization or HTTP acceptance test.
