# samples

> Minimal, runnable proof of the platform

---

## 🎯 Purpose

Show the full flow:

```text
Contract → Producer → OpenAPI → Client → Consumer
```

Nothing more.

---

## 🏗️ Structure

```text
samples
├── domain-contracts
├── spring-boot-3
└── spring-boot-4
```

Each stack contains:

```text
customer-service (producer)
customer-service-client (client)
customer-service-consumer (consumer)
```

---

## ⚡ What to check

```bash
curl http://localhost:8085/customer-service-consumer/customers/1
```

Expected:

```json
{
  "data": { ... },
  "meta": { ... }
}
```

---

## 🐳 Run

```bash
cd spring-boot-3
docker compose up --build
```

(Same for spring-boot-4)

---

## 🧠 Mental model

```text
Producer defines contract
OpenAPI is projection
Client preserves it
Consumer exposes it unchanged
```