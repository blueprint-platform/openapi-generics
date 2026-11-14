# Build topology verification

Use [multi-spec-maven-client](multi-spec-maven-client/README.md) when one Maven library consumes
multiple specifications. It proves that independent generation executions contribute usable Java
source trees to a single artifact without deleting or overwriting one another.

This family checks output/package isolation, execution-scoped BYOC (Bring Your Own Contract) and source compilation. It has no
producer/consumer runtime and does not duplicate the type or transport verification matrices.
Committed input specs are projections owned by their producer samples; Java contracts remain authoritative.

After [platform setup](../README.md#repository-build-and-local-runtime-setup), from repository root:

```bash
mvn -f samples/pom.xml -pl generation-coverage/multi-spec-maven-client -am clean verify
```

Select the actual client module, not only its aggregator, when running a targeted reactor build.
See the child README for output paths and the compile-time probe.
