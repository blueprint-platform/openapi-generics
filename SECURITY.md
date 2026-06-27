# Security Policy

We take security seriously and appreciate responsible vulnerability disclosures.

If you believe you have found a security issue in OpenAPI Generics, please report it privately using one of the channels below.

---

## Table of Contents

- [Supported Versions](#supported-versions)
- [Reporting a Vulnerability](#reporting-a-vulnerability)
- [Disclosure Process](#disclosure-process)
- [Scope](#scope)
- [Out of Scope](#out-of-scope)
- [Questions](#questions)

---

## Supported Versions

Security fixes are provided for the latest stable release line and the `main` branch.

| Version | Status |
|---------|--------|
| `main` | Active development |
| `1.2.x` | Supported |
| `1.1.x` | End of support |
| `1.0.x` | End of support |
| `< 1.0.0` | Not supported |

> **Note**
> Please upgrade to the latest stable release before reporting a security issue. Security fixes are provided only for the current supported release line.

---

## Reporting a Vulnerability

**Please do not open a public GitHub issue for security reports.**

Use one of the following private channels:

### GitHub Security Advisory (preferred)

https://github.com/blueprint-platform/openapi-generics/security/advisories/new

### Email

**baris.sayli@gmail.com**

Please include, where possible:

- a description of the issue
- reproduction steps or a minimal proof of concept
- affected version(s)
- any relevant environment details

---

## Disclosure Process

Reported vulnerabilities are reviewed as quickly as possible.

If confirmed, we will:

- assess the impact
- prepare an appropriate fix
- publish the fix in a supported release
- coordinate public disclosure when appropriate

---

## Scope

OpenAPI Generics is a platform rather than an application.

Security reports are relevant when they affect:

- OpenAPI contract projection
- generated client correctness
- contract preservation
- custom code generation
- template behavior
- vendor extension processing

Generated code issues are investigated when they originate from OpenAPI Generics itself (templates, code generation, or contract semantics).

---

## Out of Scope

The following are generally outside the scope of this policy:

- vulnerabilities caused solely by third-party dependencies
- deployment or infrastructure misconfiguration
- example and sample applications
- consumer-specific application code
- generated code issues that originate from upstream OpenAPI Generator rather than OpenAPI Generics

---

## Questions

If you are unsure whether an issue qualifies as a security vulnerability, feel free to contact:

**baris.sayli@gmail.com**

---

Thank you for helping improve the security and reliability of OpenAPI Generics.