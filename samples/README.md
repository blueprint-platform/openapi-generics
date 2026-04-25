# samples

> Minimal, runnable proof of the platform.

This directory contains end-to-end samples that demonstrate the full lifecycle:

```text
Contract → Producer → OpenAPI → Client → Consumer
```

Run it once. See generics preserved across every layer.

---

## Table of Contents

- [What you'll see](#what-youll-see)
- [Structure](#structure)
- [Prerequisites](#prerequisites)
- [Run with Docker](#run-with-docker)
- [Verify](#verify)
- [Run locally without Docker](#run-locally-without-docker)
- [Stop and clean up](#stop-and-clean-up)
- [Ports](#ports)
- [Mental model](#mental-model)

---

## What you'll see

Two parallel stacks, identical in behavior:

| Stack | Spring Boot | Variant illustrated |
|---|---|---|
| `spring-boot-3` | 3.5.x | BYOC enabled (external `CustomerDto` reuse) |
| `spring-boot-4` | 4.0.x | Default contract generation |

Each stack runs a producer (server) and a consumer (client wrapping the generated API).
The producer publishes OpenAPI; the client is generated from it; the consumer calls the producer through the client.

---

## Structure

```text
samples
├── domain-contracts
│   └── customer-contract               # shared DTO module
├── spring-boot-3
│   ├── customer-service                # producer
│   ├── customer-service-client         # generated client
│   ├── customer-service-consumer       # consumer
│   └── docker-compose.yml
└── spring-boot-4
    ├── customer-service
    ├── customer-service-client
    ├── customer-service-consumer
    └── docker-compose.yml
```

Each stack is fully independent and runnable on its own.

---

## Prerequisites

- Docker 20+ and Docker Compose v2
- (Optional, for local builds) JDK 21 and Maven 3.9+

---

## Run with Docker

Pick a stack and run:

```bash
# Spring Boot 3 stack
cd spring-boot-3
docker compose up --build -d
```

```bash
# Spring Boot 4 stack
cd spring-boot-4
docker compose up --build -d
```

What `--build` does: rebuilds images from source. Use it the first time, or after code changes. On subsequent runs you can drop `--build`:

```bash
docker compose up -d
```

The `-d` flag runs containers in the background. To follow logs in real time:

```bash
docker compose logs -f
```

Or for a specific service:

```bash
docker compose logs -f customer-service
```

---

## Verify

Once both containers are running, hit the consumer:

```bash
# Spring Boot 3
curl http://localhost:8085/customer-service-consumer/customers/1
```

```bash
# Spring Boot 4
curl http://localhost:8095/customer-service-consumer/customers/1
```

Expected response shape:

```json
{
  "data": {
    "customerId": 1,
    "name": "...",
    "email": "..."
  },
  "meta": {
    "serverTime": "...",
    "sort": []
  }
}
```

The `data + meta` envelope is `ServiceResponse<T>`, preserved end-to-end from the producer's Java contract through OpenAPI to the generated client and out through the consumer.

You can also verify the producer directly:

```bash
# Spring Boot 3
curl http://localhost:8084/customer-service/v1/customers/1

# Swagger UI
open http://localhost:8084/customer-service/swagger-ui/index.html

# Generated OpenAPI spec
curl http://localhost:8084/customer-service/v3/api-docs.yaml
```

(Use `8094` instead of `8084` for Spring Boot 4.)

Pagination, which exercises the nested generic `ServiceResponse<Page<T>>`:

```bash
curl "http://localhost:8085/customer-service-consumer/customers?page=0&size=5"
```

---

## Run locally without Docker

Build the full samples reactor:

```bash
# From the samples/ directory
mvn clean install
```

Run a single stack:

```bash
# Spring Boot 3
cd spring-boot-3
mvn clean install
```

Run an individual service:

```bash
cd spring-boot-3/customer-service
mvn spring-boot:run
```

```bash
# In another terminal
cd spring-boot-3/customer-service-consumer
mvn spring-boot:run
```

When running locally, the consumer expects the producer at `http://localhost:8084/customer-service` by default. Override with `CUSTOMER_API_BASE_URL` if needed.

---

## Stop and clean up

```bash
# Stop containers, keep images and volumes
docker compose down
```

```bash
# Stop and remove built images too
docker compose down --rmi local
```

```bash
# Full cleanup (containers, images, volumes, orphans)
docker compose down --rmi local --volumes --remove-orphans
```

---

## Ports

| Service | Spring Boot 3 | Spring Boot 4 |
|---|---|---|
| Producer (`customer-service`) | `8084` | `8094` |
| Consumer (`customer-service-consumer`) | `8085` | `8095` |

Override with environment variables in your shell or `.env`:

```bash
APP_PORT=9000 CONSUMER_APP_PORT=9001 docker compose up --build -d
```

---

## Mental model

```text
Producer defines the contract.
OpenAPI is its projection.
Client preserves it.
Consumer exposes it unchanged.
```

If the response shape your consumer returns is the same as the one your producer declared in Java, the platform is doing its job. That is the entire point of these samples.

For the underlying architecture, see the [main project README](../README.md) and [adoption guides](../docs/).