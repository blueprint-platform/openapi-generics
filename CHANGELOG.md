# Changelog

All notable changes to this project will be documented in this file.

This project follows a contract-first release discipline:

- MAJOR — contract reset
- MINOR — backward-compatible capability expansion
- PATCH — bug fixes, dependency alignment, documentation, and build/release hygiene

---

## [1.0.1] - 2026-05-01

### Patch release

OpenAPI Generics 1.0.1 is a maintenance and release-hygiene update for the 1.0.x GA line.

No contract changes.  
No public API behavior changes.  
No generated client structure changes.

Fully compatible with the 1.0.0 GA contract.

### Changed

- Refined Maven POM hygiene across platform modules.
- Kept the root aggregator lifecycle-safe while preserving shared plugin management.
- Clarified build ownership between the root aggregator, platform modules, and the codegen parent.
- Updated OpenAPI Generator alignment to 7.22.0.
- Upgraded Spring Boot baseline from 3.5.13 to 3.5.14 across platform modules and samples.
- Documented upstream template governance and fail-fast patch validation.
- Strengthened dependency alignment and explicitly controlled transitive versions where necessary.

### Quality & Verification

- Added JaCoCo and Codecov integration.
- Added unit and edge-case coverage for server-side projection and wrapper enrichment behavior.
- Verified Spring Boot 3 and Spring Boot 4 sample pipelines.

---

## [1.0.0] - 2026-04-18

### General Availability

OpenAPI Generics 1.0.0 marks the General Availability release of the contract-first generics platform.

### Highlights

- Contract-first architecture with Java as the source of truth.
- OpenAPI treated strictly as a projection layer.
- Generics-aware client generation.
- Bring Your Own Envelope (BYOE).
- Bring Your Own Contract (BYOC).
- Deterministic contract-to-spec-to-client pipeline.
- Spring Boot 3 and Spring Boot 4 sample coverage.

### Compatibility

- Java 17+
- Spring Boot 3.x / 4.x
- OpenAPI Generator 7.x
- Maven-based client generation