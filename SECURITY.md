# Security Policy

We take security seriously and appreciate responsible disclosures.
If you believe you've found a vulnerability, **please follow the process below**.

---

## Table of Contents

* [Supported Versions](#supported-versions)
* [Reporting a Vulnerability](#reporting-a-vulnerability)
* [Our Process & Timelines](#our-process--timelines)
* [Severity Guidance](#severity-guidance)
* [Coordinated Disclosure](#coordinated-disclosure)
* [Scope](#scope)
* [Non-qualifying Reports](#non-qualifying-reports)
* [Questions](#questions)

---

## Supported Versions

We provide security fixes for the latest stable release line and the `main` branch.

| Version   | Status                       |
|-----------|------------------------------|
| `main`    | Active development           |
| `1.0.x`   | Supported (security fixes)   |
| `< 1.0.0` | Not supported                |

> **Note**
> We strongly recommend running the latest stable `1.x` release before reporting issues.
> Older `1.x` patch versions may miss recently fixed vulnerabilities.

---

## Reporting a Vulnerability

**Do not open a public issue.**

Use one of the following **private disclosure channels**:

### 1. GitHub Security Advisory (preferred)

Use GitHub's private vulnerability reporting:
[Report a vulnerability](https://github.com/blueprint-platform/openapi-generics/security/advisories/new)

### 2. Email

Send details to **baris.sayli@gmail.com** with the subject prefix `SECURITY: <short summary>`.

Please include:

* A clear description of the issue and its potential impact
* A minimal proof-of-concept (PoC) or reproduction steps
* Affected version(s) (tag or commit hash)
* Environment details if relevant
* Suggested remediation ideas (optional but welcome)

---

## Our Process & Timelines

We aim to handle reports responsibly, transparently, and without unnecessary delay.

* **Acknowledgement:** within 72 hours
* **Initial triage:** within 7 days
* **Fix timeline:** depends on severity (see [Severity Guidance](#severity-guidance))
* **Release:** fixes are published once validated

For sensitive issues, **coordinated disclosure** may be used.
Reporters are kept informed at key milestones.

---

## Severity Guidance

We follow a pragmatic, CVSS-inspired classification. Severity directly influences prioritization and release timing.

### Critical

* Remote code execution
* Unsafe deserialization leading to RCE
* Authentication/authorization bypass in generated code

**Target fix:** within 30 days

### High

* Arbitrary file write/read
* Contract bypass enabling unsafe execution paths
* Significant information disclosure

**Target fix:** within 30 days

### Medium

* Schema manipulation leading to incorrect client/server behavior
* DoS within bounded system scope
* Minor information disclosure

**Target fix:** within 90 days

### Low

* Hardening gaps
* Misconfigurations
* Edge-case misuse without realistic exploit chain

**Target fix:** planned for next release cycle

---

## Coordinated Disclosure

* We prefer **coordinated disclosure**
* Please do not share details publicly before a fix is released
* Reporters may be credited in release notes upon request

---

## Scope

### In scope

This repository is a **platform**, not an application. Security concerns are evaluated across the full platform chain.

**User-facing entry points:**

* `openapi-generics-server-starter` (server side) — Spring Boot integration, contract → OpenAPI projection
* `openapi-generics-java-codegen-parent` (client side) — generator configuration, templates, and build wiring

**Transitively pulled (part of the runtime and generation chain):**

* `openapi-generics-contract` — core shared model, `ServiceResponse<T>` semantics, pagination (`Page`, `Meta`), error model (RFC 9457)
* `openapi-generics-java-codegen` — generator implementation, template behavior, type mapping and suppression logic

Consumers typically depend on only one entry point directly. The rest is pulled transitively, so security must be evaluated across the full chain.

**Security-relevant areas:**

* Misalignment between contract, OpenAPI projection, and generated code
* Contract violations or ambiguity (`ServiceResponse<T>` semantics)
* Incorrect schema projection (server → OpenAPI)
* Incorrect code generation (OpenAPI → client)
* Template-level behavior and transformation rules
* Loss of determinism between contract, spec, and generated code

### Out of scope

* Example or sample applications outside the core platform
* Vulnerabilities caused solely by **third-party dependencies** (report upstream first)
* Deployment-specific misconfigurations
* Runtime environment issues unrelated to the platform itself

---

## Non-qualifying Reports

To keep focus on impactful issues, we generally exclude:

* Best-practice recommendations without a realistic exploit scenario
* Generic rate-limiting or DoS claims without a concrete attack vector
* Missing headers or hardening suggestions in non-production contexts
* Social engineering or physical attack scenarios

### Generated code issues

Vulnerabilities found in **generated code alone** (without a corresponding template or generator flaw) are generally considered:

* **Upstream issues** — report to [OpenAPI Generator](https://github.com/OpenAPITools/openapi-generator)
* **Consumer issues** — how the generated code is used in the consumer's application

We will investigate if the generated code issue traces back to:

* Our template patches
* Our custom codegen implementation
* Contract-level semantic misalignment

> **Important**
> Generated code is treated as **disposable output**.
> Security fixes must target **contracts, templates, or generators**, not generated artifacts.

---

## Questions

If you're unsure whether something qualifies as a security issue, contact **baris.sayli@gmail.com**.

We're happy to help triage before a formal report.

---

Thank you for helping keep the platform and its users safe.

**Security, like API contracts, must be enforced at system boundaries — not patched after the fact.**