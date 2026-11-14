# ApiResponse shape coverage

Inspect `ApiResponse<T>` projection, generated transport bindings and consumer type compatibility.
This is a focused regression harness, not a canonical business application.

The application owns `ApiResponse<T>` with status/message/data/errors and its `ApiError` values.
Producer configuration selects that envelope once and emits `x-api-wrapper-type`; the client does
not repeat an envelope option. This is the core BYOE (Bring Your Own Envelope) ownership path, not a BYOC (Bring Your Own Contract) mapping.

## Shape and endpoint inventory

Paths are relative to the producer or consumer base URL below; braces list alternative path segments.
Values/enums/DTOs are representative, not a claim of arbitrary generic-graph support.

| Family | Paths | Payloads |
| --- | --- | --- |
| Scalars | `/types/scalars/{string,boolean,integer,long,decimal}` | String / Boolean / Integer / Long / BigDecimal |
| Values | `/types/values/{uuid,date,datetime,enum}` | UUID / LocalDate / OffsetDateTime / CoverageStatus |
| Objects | `/types/objects/{address,profile}` | AddressDto / TypeProfileDto |
| Collections | `/types/lists/{summaries,statuses}`; `/types/sets/{summaries,statuses}` | List/Set of DTO or enum |
| Shared page | `/types/pages/{summaries,statuses}` | Platform `Page<T>` |
| Application containers | `/types/paging/{summaries,statuses}`; `/types/windows/{summaries,statuses}` | Paging<T> / Window<T> |

```yaml
openapi-generics:
  envelope:
    type: io.github.blueprintplatform.samples.typecoverage.contract.ApiResponse
  containers:
    - type: io.github.blueprintplatform.samples.typecoverage.contract.Paging
      item-property: content
    - type: io.github.blueprintplatform.samples.typecoverage.contract.Window
      item-property: items
```

## Build evidence and its limits

After [repository setup](../../README.md#repository-build-and-local-runtime-setup), run from this directory:

```bash
mvn clean verify
```

The producer's `OpenApiSnapshotGenerationTest` regenerates the committed spec and checks metadata;
review the diff, which CI rejects if uncommitted. Generated client/consumer compilation checks that
bindings, filtered infrastructure and shared container types agree. The POM does not add an automated
HTTP/deserialization test for every row in the inventory.

The consumer intentionally calls generated APIs as a test harness. Generated DTOs may appear in its
verification endpoints; they do not become shared application authorities. Use the
[canonical web stack](../../gradle/customer-service/README.md) for controller/service/adapter architecture
and automated real consumer-to-producer HTTP acceptance.

## Manual HTTP verification

From this directory, start these in separate terminals after installing sample dependencies:

```bash
mvn -f producer/pom.xml spring-boot:run
```

```bash
mvn -f consumer/pom.xml spring-boot:run
```

| Surface | URL |
| --- | --- |
| Producer | `http://localhost:8076/type-coverage/byoe-response` |
| Consumer | `http://localhost:8077/type-coverage/byoe-response-consumer` |
| OpenAPI | Producer base + `/v3/api-docs.yaml` |
| Swagger UI | Producer base + `/swagger-ui/index.html` |

```bash
curl http://localhost:8076/type-coverage/byoe-response/types/pages/summaries
curl http://localhost:8077/type-coverage/byoe-response-consumer/types/pages/summaries
curl http://localhost:8077/type-coverage/byoe-response-consumer/types/paging/summaries
```

These calls inspect runtime JSON through the generated client; they do not regenerate it or prove
exact Java class identity merely from equivalent JSON. Shared contracts remain authoritative,
and wrapper subclasses are concrete transport bindings. [Client adoption](../../../docs/adoption/client-side-adoption.md)
explains optional DTO reuse and the application boundary.
