## Change and outcome

Describe the concrete problem, resulting behavior and affected module(s). For observable OpenAPI,
generated-source or runtime changes, include a representative before/after and explain intentional drift.

## Contract / ownership impact

Does this affect Java contracts, producer metadata, reconstruction, build integration or only documentation?
Keep BYOE (Bring Your Own Envelope) envelope ownership separate from optional BYOC (Bring Your Own Contract) DTO reuse. Ordinary upstream generation and
transport remain upstream-owned; generated wrappers are transport bindings behind application adapters.

## Validation

List the commands actually run and their results. Distinguish unit/fixture wiring, real generation and
compilation, snapshot checks and runtime HTTP tests. Note checks not run and why.
See [CONTRIBUTING.md](CONTRIBUTING.md#testing) for verification commands.

- [ ] Scope is focused; changes belong to the owning module.
- [ ] Tests/docs updated for behavior changes; unexplained spec/output drift is not accepted.
- [ ] No generated Java/build output committed; intentional committed specs reviewed.
- [ ] Consumer version ownership and supported/minimum/current/negative distinctions preserved.

Related issue/discussion (if any):
