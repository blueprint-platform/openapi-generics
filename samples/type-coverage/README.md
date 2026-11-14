# Generic shape verification

Use these harnesses to inspect projection metadata and generated bindings across many response shapes.
For an application architecture to copy, start with the [customer web stacks](../README.md).

| Harness | Envelope owner | Containers |
| --- | --- | --- |
| [service-response](service-response/README.md) | Platform `ServiceResponse<T>` | List/Set/Page plus application `Window<T>` and `Batch<T>` |
| [byoe-response](byoe-response/README.md) | Application `ApiResponse<T>` | List/Set/Page plus application `Paging<T>` and `Window<T>` |

Each has `contract`, `producer`, `client`, `consumer` and committed `spec` inputs. Producer snapshot
tests run in Maven; generated client and consumer compilation check the selected Java shapes.
Consumers expose HTTP routes for manual runtime probes. There is **not** an automated HTTP round trip
for every shape in `clean verify`; test existence, compilation and runtime execution are separate evidence.

The harnesses deliberately reference generated DTOs/APIs to exercise codegen. Those types are test
subjects, not recommended application contracts. Shared envelope/container types retain authority;
real applications should keep generated transport details behind a client adapter.

## Run the checks

After [platform/sample setup](../README.md#repository-build-and-local-runtime-setup), from repository root:

```bash
mvn -f samples/type-coverage/pom.xml clean verify
```

For the complete maintained Maven path use `mvn -f samples/pom.xml clean verify`. To target a single
family without relying on aggregator selection, use `mvn -f samples/type-coverage/service-response/pom.xml clean verify`
or its `byoe-response` equivalent after installing the required platform artifacts.

Review generated spec diffs before accepting snapshot changes. Async coverage is representative MVC
unwrapping, not arbitrary nested generic or reactive support. BYOE (Bring Your Own Envelope) selects the producer envelope;
optional BYOC (Bring Your Own Contract) DTO reuse is a separate client concern and is not required by these harnesses.

[Compatibility scope](../../docs/compatibility.md#contract-scope) ·
[Runtime application acceptance](../gradle/customer-service/README.md#build-and-acceptance-verification)
