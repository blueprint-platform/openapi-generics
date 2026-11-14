# Documentation visuals

Assets are grouped by purpose. Keep editable sources and any required exported files together:

```text
docs/images/
├── architecture/
│   └── openapi-generics-architecture.svg   # Editable source + documentation render
├── proof/
│   └── generic-binding-comparison.svg     # Editable source + documentation render
├── social/
│   ├── social-preview.svg                 # Editable source
│   └── social-preview.png                 # Final GitHub upload image
└── README.md                              # Asset inventory and reproduction
```

| Purpose | Reference / destination | Editable source | Final render |
| --- | --- | --- | --- |
| Architecture / projection and reconstruction flow | [Architecture guide](../architecture/architecture.md#architecture-overview) | [openapi-generics-architecture.svg](architecture/openapi-generics-architecture.svg) | Same SVG, displayed directly at 1280×940 |
| Before/after generated-binding proof | [Root README, “See the proof”](../../README.md#see-the-proof) | [generic-binding-comparison.svg](proof/generic-binding-comparison.svg) | Same SVG, displayed directly at 1280×620 |
| GitHub repository Social preview | **Manually uploaded by the maintainer in GitHub repository settings**, not embedded in the README | [social-preview.svg](social/social-preview.svg) | **[social-preview.png](social/social-preview.png)**, 1280×640 |

The SVGs are directly editable, self-contained source assets with accessible titles/descriptions and
local font fallbacks. They are not generated from a separate HTML, draw.io or Mermaid source. Browser
rendering produces the Social preview PNG; the two documentation SVGs need no duplicate PNG exports.
Code excerpts are shortened for reading, not screenshots of complete generated files.

### Updating an asset

- **Architecture:** edit the SVG directly and check its flow and ownership labels against the
  [architecture guide](../architecture/architecture.md). Open the SVG in a browser to render it.
- **Proof:** regenerate and inspect the Java evidence using [the steps below](#reproduce-the-comparison),
  then edit the SVG excerpts if the verified output changes. Open the SVG in a browser to render it.
- **Social preview:** edit the SVG and [regenerate its PNG](#render-the-upload-image) together.
  Keep both in the repository. The maintainer uploads the final PNG separately; no repository script
  changes GitHub settings, and committing the files does not update the live preview.

An asset's destination may be external to Markdown. In particular, the Social preview is maintained
for manual upload even though no public guide embeds it. Check this inventory before removing assets.

## README first impression and Social preview

The README leads with a short pitch and the concrete Java → metadata → binding example. Keep that
content near the top rather than repeating it inside a large hero. The comparison image belongs in
the proof section; the architecture image belongs in the architecture guide.

The [Social preview](social/social-preview.png) introduces the product when a repository link is shared
without the README. Its [editable SVG](social/social-preview.svg) leads with preserving Java response
generics across OpenAPI, then illustrates the same envelope/container types on both sides. It needs no
knowledge of Spring MVC projection, vendor-extension keys or project-specific abbreviations.
Envelope selection and optional DTO reuse belong in adoption/architecture explanations, not the card.
The example is illustrative, not a promise of arbitrary generic-graph support or replacement of ordinary
OpenAPI Generator DTO/API/transport generation.

### Previous cover and GitHub setting

The previous `cover.html` / `cover.png` pair was introduced in `1ffb109`. At review, the README on `main`
(`223e658`) embedded that PNG at the top. GitHub's custom Social preview displayed the old
cover design; it is a separately uploaded image, not a live reference to the file in git.
The old HTML specifies a 1200×630 social-share frame. This is why the old asset was serving both roles,
and why removing its README reference did not make it unused.

The replacement starts from a small SVG instead of carrying forward the old HTML/CSS, decorations
and “zero duplication / zero drift” copy. Its navy/teal palette matches the proof and architecture
visuals. Upload **[social-preview.png](social/social-preview.png)** as the repository's GitHub Social
preview image when adopting it. Adding or merging the file does not update that repository setting.

### Render the upload image

From the repository root, with Chrome installed (substitute your Chromium binary on other systems):

```bash
chrome_bin="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
preview_profile=$(mktemp -d)
"$chrome_bin" --headless=new --disable-gpu --hide-scrollbars \
  --no-first-run --no-default-browser-check \
  --user-data-dir="$preview_profile" \
  --window-size=1280,640 \
  --screenshot="$PWD/docs/images/social/social-preview.png" \
  "file://$PWD/docs/images/social/social-preview.svg"
```

The PNG uses GitHub's [recommended 1280×640 size](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/customizing-your-repositorys-social-media-preview).
Check it at full and half size. The project name and identity-preservation message should
remain clear at link-card size; the full metadata/code examples are available in the README.

## Version and verification lifecycle

The three SVGs describe product value, ownership or concrete binding behavior. None embeds exact
upstream versions, even in accessibility metadata. Compatibility roles and current/minimum/negative
values remain in the [compatibility reference](../compatibility.md); the record below describes a
specific verification run, not an additional consumer version policy.

When promoting an upstream reference:

1. Update the owning build/test references and compatibility/current-reference text, including the
   README compatibility section where applicable. Run the relevant regression and comparison checks.
2. Update **the verification record below and its reproduction inputs** after successful verification.
   An upstream patch change alone does not require editing an SVG or re-exporting the Social preview.
3. If generated snippets or ownership semantics actually change, review that behavior and update only
   the affected visual. Re-export the social PNG only when its SVG changes. Keep source and final together.

The architecture has no release/reference labels and stays valid while ownership and flow stay the same.
The proof illustrates verified snippets; tool/library/options provenance lives below, not inside the image.
The Social preview is product communication, not a compatibility matrix or an executable proof.

## Reproduce the comparison

Last verified comparison inputs (both outputs compiled):

| Input | Verified value |
| --- | --- |
| OpenAPI Generator | 7.27.0, selected coherently through the Maven parent |
| Input spec | [Canonical customer snapshot](../../samples/gradle/customer-service/spec/customer-api-docs.json) |
| Library / serialization | RestClient, Boot 3 / Jackson 2 |
| Options | `useSpringBoot3=true`, `serializationLibrary=jackson`, `openApiNullable=false` |
| External DTO mappings | None: `CustomerDto` is ordinarily generated in both outputs |
| Generator comparison | Upstream `java` versus `java-generics-contract` |

This isolates shared `ServiceResponse<T>` and `Page<T>` reconstruction. These inputs can be updated
without changing the visual when the verified binding stays identical.

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

When changing any visual, render the SVG in a browser and check text at both its native size and
normal documentation width. Keep the excerpts aligned with generated Java, not just schema names.
