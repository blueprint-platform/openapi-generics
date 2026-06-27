# Contributing Guide

Thanks for your interest in improving **OpenAPI Generics**.

OpenAPI Generics is a contract-preserving OpenAPI platform for Java and Spring Boot, built around a deterministic lifecycle:

> **Java Contract → OpenAPI Projection → Deterministic Client Reconstruction**

Built with:

- Java 17+
- Spring Boot 3.4.x, 3.5.x, 4.x
- OpenAPI Generator 7.x

> Be kind, be constructive, and follow our [Code of Conduct](./CODE_OF_CONDUCT.md).

---

## Table of Contents

- [Questions & Support](#questions--support)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Project Layout](#project-layout)
- [Coding Style & Commits](#coding-style--commits)
- [Testing](#testing)
- [Architecture Principles](#architecture-principles)
- [Pull Request Checklist](#pull-request-checklist)
- [Security](#security)
- [License](#license)

---

## Questions & Support

Before opening an issue, please search existing Issues and Discussions.

Use **GitHub Discussions** for:

- design questions
- generic contracts and containers
- projection and code generation behavior
- usage and integration questions

Use **GitHub Issues** for reproducible bugs.

---

## How to Contribute

1. Fork the repository.
2. Create a focused branch.

```bash
git checkout -b feature/short-description
```

3. Keep changes small and self-contained.
4. Update documentation and tests when behavior changes.
5. Run the full build before opening a pull request.

Small, focused pull requests are easier to review.

---

## Development Setup

### Prerequisites

- Java 17+ (Java 21 recommended)
- Maven 3.9+

### Build

```bash
mvn -q -ntp clean verify
```

---

## Project Layout

```text
openapi-generics-contract
openapi-generics-platform-bom
openapi-generics-server-starter
openapi-generics-java-codegen
openapi-generics-java-codegen-parent
```

---

## Coding Style & Commits

### Principles

- Keep implementations deterministic.
- Avoid unnecessary complexity.
- Never modify generated code.
- Fix issues at the source:
  - contract
  - projection
  - generator

### Commit prefixes

- `feature:`
- `bugfix:`
- `docs:`
- `refactor:`
- `test:`
- `ci:`

---

## Testing

Run the complete verification suite before submitting changes:

```bash
mvn clean verify
```

Core platform modules focus on deterministic contract projection and client generation. End-to-end validation is performed through the sample projects.

---

## Architecture Principles

These are project invariants.

### Contract First

Contract-owned response envelopes (for example `ServiceResponse<T>` or your own BYOE envelope) define the API contract.

OpenAPI is a projection of that contract.

### Determinism

The same contract and configuration must always produce the same generated output.

### Single Source of Truth

Contracts are defined once and reused across server, OpenAPI, and generated clients.

### Generated Code Is Disposable

Never fix problems in generated sources.

Always fix them in:

- the contract
- the projection layer
- the generator

---

## Pull Request Checklist

- [ ] Scope is focused
- [ ] Build passes
- [ ] Tests updated when required
- [ ] Documentation updated when required
- [ ] No generated code has been modified

---

## Security

Please do **not** report security vulnerabilities through public GitHub issues.

Use GitHub Security Advisories or the contact information described in [SECURITY.md](./SECURITY.md).

---

## License

By contributing, you agree that your contributions will be licensed under the project's MIT License.