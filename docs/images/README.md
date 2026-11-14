# Documentation visuals

These two editable SVGs are the source artifacts; there is no separate HTML/draw.io version to keep
in sync. They use local fonts and contain accessible titles/descriptions. Code excerpts are shortened
for reading, not screenshots of complete generated files.

| Visual | Purpose | Used by |
| --- | --- | --- |
| [Generic binding comparison](proof/generic-binding-comparison.svg) | Show how the same response schema becomes fields with upstream `java`, or a subclass of the shared envelope/container with `java-generics-contract` | [Root README](../../README.md#see-the-proof) |
| [Architecture](architecture/openapi-generics-architecture.svg) | Separate Java authority, OpenAPI projection, reconstruction and application boundaries; distinguish optional DTO reuse from envelope ownership | [Architecture guide](../architecture/architecture.md#architecture-overview) |

## Reproduce the comparison

The comparison was checked with **OpenAPI Generator 7.27.0**, **RestClient**, Boot 3/Jackson 2 options
and no external DTO mappings. Both outputs compiled. `CustomerDto` is ordinarily generated in both;
this comparison isolates shared `ServiceResponse<T>` and `Page<T>` reconstruction.

1. Install the root Maven reactor (`mvn clean install`).
2. Make two temporary copies of the [transport client POM](../../samples/transport-coverage/client/pom.xml),
   without its handwritten `src` tree. Set the parent `relativePath` to empty so the installed parent is used.
3. Point both `inputSpec` values at the same [customer snapshot](../../samples/gradle/customer-service/spec/customer-api-docs.json).
   Keep `library=restclient`, `useSpringBoot3=true`, `serializationLibrary=jackson`,
   `openApiNullable=false` and `openapi-generator.version=7.27.0`.
4. Set `generateApiDocumentation`, `generateApiTests`, `generateModelDocumentation` and
   `generateModelTests` to `false` in both copies. Remove any `openapi-generics.response-contract.*`
   properties; DTO reuse is deliberately disabled.
5. For the stock copy select `generatorName=java`, set `openapi.generics.skip=true`, and set
   `templateDirectory` to an empty directory so the inherited platform overlay is not used.
6. For the binding copy select `generatorName=java-generics-contract`; retain the parent template
   lifecycle and add the platform `openapi-generics-contract:1.3.0` dependency.
7. Run `mvn clean compile` in each temporary project. Inspect
   `target/generated-sources/openapi/src/gen/java/.../generated/dto/ServiceResponsePageCustomerDto.java`.

The stock file declares `PageCustomerDto data` and `Meta meta`. The binding file declares
`extends ServiceResponse<Page<CustomerDto>>`. The visual omits imports, annotations, accessors and
other boilerplate; it does not imply that all stock model fields are shown or that API/transport
code has moved into platform ownership.

When changing either visual, render the SVG in a browser and check text at both its native size and
normal documentation width. Keep the excerpts aligned with generated Java, not just schema names.
