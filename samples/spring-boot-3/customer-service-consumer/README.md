# Boot 3 customer consumer web application

Expose customer operations through your own controller/service boundary while delegating remote calls
to the reusable client library:

```text
CustomerConsumerController → CustomerConsumerService
  → CustomerServiceClient → CustomerClientAdapter → generated RestClient → producer
```

The consumer owns request DTOs and `CustomerRequestMapper`; the client library owns generated
API/invoker setup and response DTO mapping. Read responses use shared `ServiceResponse`, `Page` and
application `CustomerDto` types. This is an application layering example, not permission to use
generated wrapper classes as domain contracts.

## Run and call

Install sample dependencies using the [sample setup](../../README.md#repository-build-and-local-runtime-setup).
Start the [producer](../customer-service/README.md), then run `mvn spring-boot:run` here.

```bash
curl http://localhost:8085/customer-service-consumer/customers/1
```

The default upstream is `http://localhost:8084/customer-service`. Override it through
`CUSTOMER_API_BASE_URL` / `customer.api.base-url`; consumer port/context path are in `application.yml`.


## Errors and verification

`CustomerConsumerExceptionHandler` maps the library's `ApiProblemException` to ProblemDetail and owns
its generic internal-error response. The consumer does not parse generated HTTP exception types.

Configured tests exercise consumer mapping/delegation; use real application calls above to inspect the
whole chain. Shared success-response JSON alone does not prove producer OpenAPI projection. The
[canonical Gradle acceptance tests](../../gradle/customer-service/README.md#build-and-acceptance-verification)
provide a separate automated consumer-endpoint-to-producer HTTP path.
