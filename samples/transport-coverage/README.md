# RestClient transport interoperability

Check ordinary upstream multipart, binary download and form-urlencoded behavior alongside generic
`ServiceResponse<T>` reconstruction. The platform does not supply custom transport templates or take
ownership of HTTP serialization merely because this harness exercises it.

## Routes and modules

| Scenario | Producer route | Result |
| --- | --- | --- |
| Multipart file + JSON metadata | `POST /transport/multipart` | `ServiceResponse<UploadResultDto>` |
| Binary | `GET /transport/binary/{documentId}` | Spring `Resource` / octet stream |
| Form-urlencoded | `POST /transport/form` | `ServiceResponse<RegistrationResultDto>` |

`contract` owns producer DTOs; `producer` publishes routes and the committed spec. `client` generates
RestClient and exposes `TransportCoverageClientAdapter`; `consumer` delegates through its service/client
layer to that adapter. Ordinary generated DTOs are used deliberately in this transport harness;
[application samples](../README.md) show stable domain/API boundaries instead.

## Build and test scope

After [platform/sample setup](../README.md#repository-build-and-local-runtime-setup), from repository root:

```bash
mvn -f samples/transport-coverage/pom.xml clean verify
```

This runs producer snapshot generation and generated client/consumer compilation. Review spec drift.
The producer's `TransportControllerIT` is **not** selected by default Surefire or bound to Failsafe in
its POM. Run its server-side HTTP checks explicitly:

```bash
mvn -f samples/transport-coverage/producer/pom.xml -Dtest=TransportControllerIT test
```

Those tests do not establish a generated-client multipart/form/binary round trip. Use the manual
consumer calls below to inspect that runtime route. Dedicated canonical Gradle HTTP acceptance covers
direct/page JSON responses, not this transport matrix.

## Run both applications

From this directory, use separate terminals:

```bash
mvn -f producer/pom.xml spring-boot:run
```

```bash
mvn -f consumer/pom.xml spring-boot:run
```

Producer: `http://localhost:8078/transport-coverage`; consumer:
`http://localhost:8079/transport-coverage-consumer`. Inspect producer `/v3/api-docs.yaml` or
`/swagger-ui/index.html` separately from normal HTTP responses.

## Probe the actual transport paths

From this sample directory (which contains `README.md`), upload a real file directly to the producer:

```bash
curl -X POST http://localhost:8078/transport-coverage/transport/multipart \
  -F 'file=@README.md;type=text/plain' \
  -F 'metadata={"description":"multipart regression","category":"contract"};type=application/json'
```

The consumer endpoint creates the sample bytes/metadata and delegates the multipart request:

```bash
curl -X POST 'http://localhost:8079/transport-coverage-consumer/transport/multipart?fileName=contract.txt&description=multipart%20regression&category=contract'
curl http://localhost:8079/transport-coverage-consumer/transport/binary/sample
curl -X POST 'http://localhost:8079/transport-coverage-consumer/transport/form?name=blueprint&category=platform'
```

Compare with a direct form call:

```bash
curl -X POST http://localhost:8078/transport-coverage/transport/form \
  -H 'Content-Type: application/x-www-form-urlencoded' --data 'name=blueprint&category=platform'
```

These are interoperability probes, not a new platform transport contract. Application-owned envelope/custom-container,
request-side generic reconstruction and reactive transport scenarios remain outside this harness.
