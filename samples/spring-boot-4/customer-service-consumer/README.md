# customer-service-consumer

> Minimal reference: consume a generated client and expose it without breaking the contract

---

## 🎯 Purpose

Show the **last step**:

```text
Producer → OpenAPI → Client → Consumer
```

Goal:

* use generated client safely
* keep `ServiceResponse<T>` intact
* expose it directly

---

## ⚡ How it works

```text
Controller → Service → Client → Adapter → Generated API
```

Key point:

> Application never talks to generated code directly

---

## 🔌 Boundary (important)

```text
CustomerServiceClient
```

Responsibilities:

* delegates to adapter
* isolates generated client
* keeps contract types

---

## ⚖️ Errors

Upstream errors:

```text
ProblemDetail → ApiProblemException
```

Handled in controller advice.

No wrapping.

---

## 🔄 Contract

Always preserved:

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

No mapping. No duplication.

---

## ⚙️ Config

```yaml
customer:
  api:
    base-url: http://localhost:8094/customer-service
```

---

## 🧪 Run

```bash
mvn spring-boot:run
```

```bash
curl http://localhost:8095/customer-service-consumer/customers/1
```

---

## 🧠 Mental model

```text
Adapter is the boundary
Contract flows unchanged
```