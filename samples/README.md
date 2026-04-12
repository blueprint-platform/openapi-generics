# samples

> Reference playground for the openapi-generics platform — demonstrating contract, projection, and generation end-to-end

---

## 📑 Table of Contents

* [🎯 What this directory is for](#-what-this-directory-is-for)
* [🏗️ Structure](#-structure)
* [🧠 Key idea](#-key-idea)
* [📦 domain-contracts](#-domain-contracts)
* [🔄 Spring Boot 3 & 4 pipelines](#-spring-boot-3--4-pipelines)
* [🐳 Running with Docker Compose](#-running-with-docker-compose)
* [🧪 What to verify](#-what-to-verify)
* [🔑 Important notes](#-important-notes)
* [🧾 Summary](#-summary)

---

## 🎯 What this directory is for

The `samples` module provides **working, minimal reference implementations** of the full platform pipeline:

```text
Contract → Producer → OpenAPI → Client → Consumer
```

It exists to answer one question:

> How does this actually run in a real setup?

---

## 🏗️ Structure

```text
samples
├── domain-contracts
│   └── customer-contract
│
├── spring-boot-3
│   ├── customer-service
│   ├── customer-service-client
│   └── customer-service-consumer
│
└── spring-boot-4
    ├── customer-service
    ├── customer-service-client
    └── customer-service-consumer
```

---

## 🧠 Key idea

Each stack (SB3 / SB4) contains a **complete pipeline**:

```text
Producer → OpenAPI → Generated Client → Consumer
```

All modules are intentionally minimal and focused on **correct structure**, not features.

---

## 📦 domain-contracts

Contains shared domain models.

Example:

```text
CustomerDto
```

This module represents:

> The external contract reused across producer and client

---

## 🔄 Spring Boot 3 & 4 pipelines

Each version demonstrates the same architecture with different runtime stacks.

### Producer

```text
customer-service
```

* exposes API
* returns `ServiceResponse<T>`
* produces deterministic OpenAPI

---

### Client

```text
customer-service-client
```

* generates contract-aligned client
* preserves generics
* reuses contract models

---

### Consumer

```text
customer-service-consumer
```

* consumes generated client
* exposes API again
* preserves contract end-to-end

---

## 🐳 Running with Docker Compose

Each stack includes a compose setup.

### Spring Boot 3

```bash
cd spring-boot-3

docker compose up --build
```

Services:

* customer-service → [http://localhost:8084/customer-service](http://localhost:8084/customer-service)
* customer-service-consumer → [http://localhost:8085/customer-service-consumer](http://localhost:8085/customer-service-consumer)

---

### Spring Boot 4

```bash
cd spring-boot-4

docker compose up --build
```

Services:

* customer-service → [http://localhost:8094/customer-service](http://localhost:8094/customer-service)
* customer-service-consumer → [http://localhost:8095/customer-service-consumer](http://localhost:8095/customer-service-consumer)

---

## 🧪 What to verify

Example request:

```bash
curl http://localhost:8085/customer-service-consumer/customers/1
```

Expected shape:

```json
{
  "data": { ... },
  "meta": { ... }
}
```

This confirms:

```text
Contract → OpenAPI → Client → Consumer is aligned
```

---

## 🔑 Important notes

* Samples are **not production systems**
* They are intentionally minimal
* They exist only to demonstrate:

```text
Correct architecture + correct flow
```

---

## 🧾 Summary

```text
Samples = runnable proof of the platform
```

They show how contract semantics move through the system **without duplication or drift**.
