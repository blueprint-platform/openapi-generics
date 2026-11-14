# Boot 3 customer producer

A Boot MVC application exposing customer CRUD/search through shared `ServiceResponse<T>` and
`ServiceResponse<Page<T>>` responses. Controllers call the service layer; the server starter projects
those Java contracts into the committed `../spec/customer-api-docs.yaml`.

## Run and inspect

First follow the [sample build prerequisites](../../README.md#repository-build-and-local-runtime-setup).
From this module, run `mvn spring-boot:run` (use JDK 21+).

```bash
curl http://localhost:8084/customer-service/customers/1
curl http://localhost:8084/customer-service/v3/api-docs.yaml
```

Swagger UI: `http://localhost:8084/customer-service/swagger-ui/index.html`.
The producer base path/port come from `src/main/resources/application.yml`.
This Boot 3 stack does not use the Boot 4 API-version header routing.

## Follow the contract

The Java customer DTO belongs to [customer-contract](../../domain-contracts/README.md), while the
platform owns the built-in envelope/page types. The starter publishes envelope/container identity;
it does not intercept requests or own the business/error handlers. Error responses use ProblemDetail,
not an invented generic success-envelope error shape.

A successful `/customers/1` response tests normal HTTP behavior. Separately inspect wrapper metadata
and review snapshot drift to establish projection correctness, then compile/test the
[client library](../customer-service-client/README.md).

This sample chooses the built-in envelope. Existing application envelopes are supported through
[producer BYOE (Bring Your Own Envelope) configuration](../../../docs/adoption/server-side-adoption.md#choose-the-envelope);
this is an ownership choice, not a requirement to replace every envelope with ServiceResponse.
