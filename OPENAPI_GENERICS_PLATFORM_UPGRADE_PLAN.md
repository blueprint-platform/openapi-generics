# OpenAPI Generics Platform Upgrade — Analiz ve Çalışma Planı

Son güncelleme: 2026-11-14. İncelenen çalışma ağacı: `1.3.0-SNAPSHOT`.

Bu dosya repository'nin tek yaşayan upgrade/release planıdır. Ayrı Gradle architecture review ve Maven/Gradle publication notunun kalıcı sonuçları burada birleştirildi. Gradle version-role ayrımı, OAG 7.27.0 / Gradle 9.9.1 promotion'ları ve release review'de bulunan F1/F2 düzeltmeleri tamamlandı; final Gradle verification yeşil. Maven/core ve sample reference alignment'ı ile production publication hazırlığı ayrı kalan işlerdir.

Supported line, minimum compatibility edge, current verified/reference, negative boundary ve temporary candidate farklı rollerdir. Concrete consumer version seçimi consumer'a, integration coherence platforma aittir. Aşağıda mevcut implementation, tamamlanmış verification ve **planned/intended publication architecture** açıkça ayrılır. Adım 4 canonical Gradle stack implementation ve doğrulamaları tamamlandı; aşağıdaki kanıtlar güncellendi. Maven reference promotion ve production publication implementation henüz yapılmadı.

## Bölüm A — Repository Understanding

### 1. Project purpose

OpenAPI Generics, Java generic response contract'larının OpenAPI projection ve Java client generation sırasında kimliğini kaybetmesini önler. Standart üretim `ServiceResponseCustomerDto` gibi her kombinasyon için bağımsız envelope modelleri oluşturabilir. Platform bunun yerine shared Java envelope ve container tiplerini koruyup somut transport binding'leri üretir:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

Envelope davranışının ve alanlarının sahibi shared contract'tır. Generated sınıf generic parametreleri bağlar; yeni contract authority değildir. DTO'lar BYOC ile uygulama contract'larından alınabilir; BYOC kullanılmadığında ordinary DTO generation upstream'in sorumluluğunda kalır. Dolayısıyla “hiç model generate edilmez” doğru bir açıklama değildir.

Dayanaklar: `docs/architecture/rationale.md`, `GenericAwareJavaCodegen`, `api_wrapper.mustache`, sample client adapter'ları.

### 2. Architectural model

```text
Java contract + MVC controller signatures
  → Springdoc/OpenAPI projection + server metadata
  → OpenAPI document (iki pipeline arasındaki protocol boundary)
  → JavaClientCodegen specialization + effective templates
  → shared contract'ları kullanan generated transport binding
  → client adapter → consumer application
```

**Contract:** `openapi-generics-contract` saf Java 17 tipleri sağlar. `ServiceResponse<T>` mutable `data` ve `Meta` tutar; framework annotation/dependency taşımaz. `Page<T>` bir record'dur; `List<T> content`, sayfalama alanları ve navigation flag'leri içerir. Platform `Page` ile Spring Data `Page` aynı tip değildir.

**Projection:** `OpenApiGenericsAutoConfiguration`, Springdoc `OpenApiCustomizer` üzerinden `OpenApiPipelineOrchestrator.run` bağlar. MVC discovery, `RequestMappingHandlerMapping` üzerinden return type'ları bulur. `ResponseIntrospectionPolicyResolver` envelope ve container descriptor'larını kurar; introspector bunlardan response descriptor üretir. `WrapperSchemaProcessor`, metadata enrichment, exclusion ve `OpenApiContractGuard` projection'ı tamamlar. Orchestrator aynı OpenAPI nesnesini identity bazında bir kez işler; bu mekanizma genel bir schema cache değildir.

**Reconstruction:** `GenericAwareJavaCodegen extends JavaClientCodegen`; adı `java-generics-contract` ve SPI kaydı vardır. `processOpts`, `fromModel`, `postProcessModels`, `postProcessAllModels` hook'larını specialize eder, upstream işleme için `super` çağırır. Generator fork'u veya komple kopyalanmış Java template set'i yoktur. Upstream `Java/model.mustache` alınır, wrapper dispatch eklenir, platformun küçük `api_wrapper.mustache` overlay'i kullanılır. Ordinary enum/oneOf/POJO dispatch korunur.

**Sorumluluk sınırı:** Server Java signature/configuration bilgisini çözer, schema ve canonical metadata yayımlar. Client controller taramaz, producer runtime'ına bağlanmaz, Java generic grafiğini runtime reflection ile tahmin etmez. OpenAPI metadata'sını doğrular; shared envelope/container import'larını ve BYOC eşlemelerini kullanır. Build-tool integration generation lifecycle'ını yönetir; contract semantics'i yeniden tanımlamaz. HTTP transport, auth ve serialization internals upstream/ecosystem sahipliğindedir.

### 3. Projection protocol ve desteklenen şekiller

| Extension | Üreten/tüketen | Gerçek anlamı |
|---|---|---|
| `x-api-wrapper` | Server → codegen/template | Boolean wrapper marker; string `"true"` ile eşdeğer değildir |
| `x-api-wrapper-type` | Server → envelope resolver | Shared envelope FQCN; wrapper başına envelope identity |
| `x-api-wrapper-datatype` | Server → validator/template | Projected payload adı; ör. `CustomerDto`, `PageCustomerDto` |
| `x-data-container` | Server → template | Generic container'ın Java'da kullanılacak semantic/simple adı |
| `x-data-container-type` | Server → template | Container Java FQCN; import kaynağı |
| `x-data-item` | Server → template/import resolver | Container içindeki item schema/type adı |
| `x-ignore-model` | Server → ignore decider | Schema dokümanda kalır; duplicate Java model generation engellenir |
| `x-envelope-import`, `x-envelope-type` | Codegen içi | `x-api-wrapper-type` üzerinden türetilen template metadata'sı |
| `x-extra-imports` | Codegen içi | BYOC mapping'den gelen external payload import'u |

Dayanak: server `WrapperSchemaMetadataApplier`, `WrapperSchemaEnricher`, `ContractSchemaExclusionApplier`; codegen `CodegenVendorExtensions`, `EnvelopeMetadataResolver`, `ExternalImportResolver` ve wrapper template.

`x-payload-property` mevcut canonical codegen extension set'inde bulunmaz. Payload property server descriptor'ında taşınır ve schema resolution/guard sırasında kullanılır. Eski instruction/doküman listelerini aktif protocol zannetmemek gerekir.

**Containers:** Built-in descriptor'larda `List` ve `Set`, `DIRECT_ARRAY` / assignable matching; platform `Page`, `OBJECT_WITH_ITEM_ARRAY` / exact matching ve `content` item property kullanır. Container metadata ve shape üzerinden ortak processing vardır; reconstruction template'inde Page'e özel branch yoktur. `Set`'in Java kimliği metadata ile korunur; yalnız array schema'sından çıkarılmaz.

**Application-defined generic containers:** Explicit `openapi-generics.containers` kaydı gerekir. `ConfiguredContainerTypesResolver` FQCN, concrete class/record, tam bir type parameter ve doğrudan declared instance field olan `List<T>` veya `Set<T>` item property şartlarını startup'ta doğrular. Inherited veya nested generic item alanlarını otomatik çözmez. Configured matching exact'tır. Sample `Window`, `Batch`, BYOE tarafındaki `Paging` bunun örnekleridir. Container Java tipi generated client compile classpath'inde bulunmalıdır.

**BYOE:** Producer `openapi-generics.envelope.type` seçer. Resolver concrete class ister; record envelope kabul edilmez. Tam bir generic parameter ve o parametre tipinde tek direct payload field gerekir; nested payload slot ve birden çok generic parameter desteklenmez. `ApiResponse<List<T>>` kullanımı, envelope tanımının `T payload` olması ve kullanım yerinde `T=List<Dto>` bağlanmasıdır. Client envelope seçimini yeniden configure etmez; `x-api-wrapper-type` okur. Producer bir policy ile seçili envelope'u introspect eder; codegen metadata modelini wrapper bazında tüketir. Codegen'in çoklu envelope okuyabilmesi producer'ın sınırsız otomatik envelope discovery yaptığı anlamına gelmez.

**BYOC:** Client `additionalProperties` içinde `openapi-generics.response-contract.<schemaName>=<FQCN>` registry'ye kaydedilir. Mapped model ignore edilir, wrapper item/direct payload için import türetilir. Shared dependency ayrıca sağlanmalıdır. `x-ignore-model` tek başına keyfi DTO'nun FQCN mapping'i değildir.

**Validation ayrıntısı:** `WrapperMetadataValidator`, wrapper marker/detail çelişkilerini, Boolean/String türlerini, boş değerleri ve FQCN biçimini kontrol eder. Container üçlüsü birlikte bulunmalıdır. Server guard wrapper varlığını, payload property'yi, datatype/container/item eşitliğini doğrular; envelope/container FQCN eşitliği için aynı kapsamda bir guard kontrolü yoktur. Client FQCN kontrolü de sınıfın classpath'te gerçekten var olduğunu doğrulamaz; compilation bu sınırı tamamlar.

**Scope sınırları:** MVC synchronous response'lar ve introspector'da `ResponseEntity`, `CompletionStage`/`Future`, `DeferredResult`, `WebAsyncTask` unwrapping bulunur (en fazla 8 katman). Hepsinin aynı düzeyde E2E coverage'ı yoktur. Generic item'ın kendisinin generic olması desteklenmez. Enum payload için `@Schema(enumAsRef=true)` beklenir. Custom `@Schema(name=...)` ile Java simple name'den farklı component adları supported scope değildir. WebFlux ve arbitrary nested generic graph reconstruction yoktur.

### 4. Module responsibilities

| Modül/alan | Sorumluluk ve bağımlılık yönü |
|---|---|
| `openapi-generics-contract` | Framework bağımsız envelope, paging, meta/sort ve error extension Java tipleri |
| `openapi-generics-server-starter` | Contract → metadata'lı OpenAPI; Spring Boot/Springdoc/MVC integration ve fail-fast server validation |
| `openapi-generics-java-codegen` | OpenAPI → Java generic reconstruction specialization; metadata validator, ignore filtering, external imports, SPI ve template overlay |
| `openapi-generics-java-codegen-parent` | Maven consumer lifecycle, upstream extraction, patch, overlay, plugin dependency alignment, source registration/normalization |
| `openapi-generics-gradle-plugin` | Aynı codegen artifact/protocol için official Gradle plugin integration; ayrı Gradle build, root Maven reactor modülü değil |
| `openapi-generics-platform-bom` | Internal contract dependency ve `springdoc-openapi-starter-common` alignment; OpenAPI Generator/plugin/wrapper version yöneticisi değil |
| `samples/domain-contracts` | BYOC customer DTO contract'ı |
| `samples/spring-boot-3` | Producer → snapshot → generated RestClient → consumer; Java 21 / Boot 3 stack |
| `samples/spring-boot-4` | Aynı lifecycle'ın Java 25 / Boot 4 compatibility reference'ı |
| `samples/type-coverage/service-response` | Built-in envelope ile scalar/value/object/list/set/page/custom-container/async projection ve generated consumer compilation |
| `samples/type-coverage/byoe-response` | BYOE ve registered container reconstruction; ayrı application contract |
| `samples/transport-coverage` | Multipart, binary, form-urlencoded interoperability örnekleri; generic JSON reconstruction ile birlikte ordinary RestClient generation |
| `samples/generation-coverage/multi-spec-maven-client` | İki isolated Maven execution'ın aynı client module'de compile edilmesi |
| `samples/gradle/customer-service` | Canonical Gradle-first Boot 4.1.2 / Java 25 stack: pure Java contract, producer, committed spec, generated RestClient client ve consumer; gerçek direct/page HTTP deserialization |

`samples/pom.xml` Gradle stack'ini çalıştırmaz. `samples/gradle/customer-service/settings.gradle.kts` plugin source'unu `includeBuild` eder; yalnız platform snapshot artifact'leri restricted `mavenLocal` üzerinden tüketilir. Shared `CustomerDto` bir Gradle project dependency'sidir; Maven customer-contract veya live producer client build prerequisite'i değildir. Published consumer bu development bootstrap modelini kopyalamamalıdır.

Canonical Gradle adoption stack'i Adım 4 kapsamında tamamlandı. Custom GenerateTask, non-default source folder ve BYOC değeri yeni `client` modülüne taşındığından eski standalone `customer-service-client` redundant bulunarak kaldırıldı. Type/transport coverage sample'larının sorumlulukları değiştirilmedi.

### 5. Maven client-generation lifecycle

`openapi-generics-java-codegen-parent/pom.xml` managed configuration'ları actual build plugin declarations ile aktive eder. Consumer spec, generatorName, library, package ve execution seçeneklerini sağlar.

1. `generate-sources`: Seçilen `openapi-generator.version` artifact'inden `Java/model.mustache` unpack edilir.
2. Aynı fazda platform codegen artifact'inden `META-INF/openapi-generics/templates/**` çıkarılır.
3. Ant regexp ile opening/closing dispatch patch edilir; wrapper marker oluşmazsa build fail eder.
4. Platform partial effective `Java` template directory'ye overlay edilir.
5. Official `openapi-generator-maven-plugin`, generator/core bağımlılıkları ve extraction aynı version property ile hizalanır; platform codegen ayrı project version'dan gelir.
6. Generator çalışır; sourceFolder varsayılanı `src/gen/java`, output `target/generated-sources/openapi`.
7. Build-helper default root'u ekler; official generator'ın source registration'ı execution-specific output'larda da önemlidir.
8. `process-sources`: Spotless Cleanthat unused-import removal + Google Java Format generated Java'yı normalize eder.

`openapi.generics.skip=true`, extraction/patch/overlay ve generated normalization'ı skip eder; generatorName'i değiştirmez. Tam stock generator opt-out'u ayrıca `java` seçmektir. Mevcut Maven patch doğrulaması Gradle patcher kadar güçlü değildir: iki anchor'ın tam bir kez bulunduğunu ve zaten patch'li input'u aynı kesinlikle kontrol etmez. Upgrade bahanesiyle bunu otomatik yeniden tasarlamak yerine ayrı risk olarak izlemek gerekir.

Multi-spec sample iki output/package root kullanır. Parent'ın default normalization include pattern'i bu iki custom output root'unu kapsamaz. Sample compilation multi-spec source registration kanıtıdır; her custom root'un normalize edildiğinin kanıtı değildir.

### 6. Gradle client-generation lifecycle

**Application boundary:** Official plugin consumer-owned kalır; OpenAPI Generics onu apply etmez. Integration nesneleri `withPlugin("org.openapi.generator", ...)` callback'i içinde oluşturulur; upstream settings/API sınıfları official runtime hazır olmadan yüklenmez. Plugin-only inert davranışı ve application order mevcut functional testlerle doğrulanır.

**Capability ve artifact alignment:** Resolvable/non-consumable `openApiGeneratorExtra` zorunludur; numeric version guessing yoktur. Platform kendi codegen artifact'ini plugin JAR manifest `Implementation-Version` ile hizalar; farklı direct declaration fail eder. Bu guard tüm transitive/substitution/resolutionStrategy override'larının final graph denetimi değildir.

**Upstream discovery:** `GenerateTask.class.getClassLoader()` üzerinden running official runtime'ın `Java/model.mustache` owner'ı bulunur. Missing/ambiguous artifact fail eder; consumer repository'sinden ikinci bağımsız upstream template version'ı resolve edilmez. CompileOnly upstream API/config binding dependency'leri consumer publication runtime pin'i değildir.

**Template lifecycle:** Tek `prepareOpenApiGenericsTemplates` task'ı classpath input/output'larıyla upstream template'i okur, exact-once opening/closing dispatch patch'i uygular ve shared codegen overlay'ini kopyalar. Already-patched/missing/ambiguous anchor ve eksik overlay explicit failure'dır. Ordinary enum/oneOf/POJO dispatch korunur.

**Selection ve F1 sonucu — tamamlandı:** GeneratorName lazy okunur; default/custom/multiple GenerateTask'lara gerekli classpath ve preparation dependency'si bağlanır. Consumer configuration tamamlandıktan sonraki project `afterEvaluate` aşamasında template ownership yalnız `java-generics-contract` task'larına uygulanır. Non-selected task'a absent template provider set edilmez; official extension templateDir bağlantısı korunur. Kalıcı regression ordinary `java` generation'da consumer custom model template sentinel'ini doğrular. Bu dar template configuration aşaması, execution-time Project lookup veya task callback'i içinde cross-task/source-set mutation değildir.

**Source registration ve F2 sonucu — tamamlandı:** Java plugin varsa registrar project-level setup'ta, GenerateTask configuration callback'i dışında main roots/compile dependency'lerini kurar. Generation, registration ve normalization aynı effective sourceFolder kullanır: `configOptions` > task `additionalProperties` > configFile > default. JSON/YAML config, upstream `DynamicSettings` binding'i ve aynı mapper modülüyle okunur; config semantics'i ayrı keyfi parser ile tanımlanmaz. Dosya içeriği Gradle `fileContents` ile izlenir; output root `locationOnly` provider'ıyla task üretimi tamamlanmadan çözülebilir. Provider zinciri execution/cache state'ine Project capture taşımaz.

**Normalization ve snapshot:** Ayrı hidden formatter configuration'ı, Cleanthat/JavaParser + JVM-aware Google Java Format sağlar; consumer Spotless Gradle plugin apply etmez. UTF-8/LF normalization selected GenerateTask'ın final action'ıdır; formatter classpath input fingerprint'e girer ve output snapshot normalize edilmiş içeriği görür. Missing-directory no-op intentional kalır; yanlış root sorunu bu no-op'u topluca harden ederek çözülmedi.

**Configuration cache:** Action state'i configuration sırasında yakalanır; execution sırasında Project/Task.extensions lookup yapılmaz. Template prep, task graph dry-run, real multi-task reuse ve configFile real generation/compile/cache replay farklı kanıt katmanlarıdır. Yeni configFile E2E'si dosya değişince cache'in yeniden değerlendirilmesini ve yeni root'un kullanılmasını da doğrular. Bunlar Isolated Projects, remote build-cache relocation veya tüm runtime kombinasyonlarının kanıtı değildir.

### 7. Compatibility boundaries ve version kaynakları

#### 7.1 Compatibility ve version ownership modeli

| Kavram | Anlamı | Bugünkü state |
|---|---|---|
| Supported line | Platformun destek politikası; her patch kombinasyonu için exhaustive test iddiası değildir | OAG **7.x**; Gradle plugin runtime **9.x** |
| Minimum compatibility edge | Alt sınırdaki integration/API veya runtime uyumluluk noktası | OAG Gradle integration **7.25.0** API/capability baseline; Gradle **9.0.0** minimum tested edge |
| Current verified/reference | Repository'nin güncel default doğrulama girdisi; consumer pin'i veya support minimumu değildir | Gradle plugin OAG **7.27.0**, wrapper/reference **9.9.1** |
| Negative boundary | Belirli integration capability'sinin bulunmadığını doğrulayan explicit regression case | OAG **7.24.0**, yalnız Gradle integration için |
| Temporary candidate | Yeni upstream sürümün current değiştirilmeden mevcut implementation ile sınandığı geçici rol | Bugün 7.27.0 / 9.9.1 için tamamlandı; şu anda açık candidate yok |
| Consumer version ownership | Supported line içinde concrete official tool/framework seçiminin consumer'da kalması | Reference promotion consumer version'ını override etmez |

OAG 7.25.0 minimum'u general platformun 7.x politikasını daraltmaz; 7.24.0 için Maven/codegen global rejection sonucu çıkarılmaz. Gradle wrapper 9.9.1, minimum tested edge olan 9.0.0'ın yerine geçmez. Minimum/negative noktaları current ilerlediği için değişmez; yalnız gerçek compatibility boundary değişirse yeniden değerlendirilir.

Kalıcı OAG verification rolleri **minimum + tek current + negative**; Gradle runtime rolleri **minimum edge + tek current** şeklindedir. Yeni upstream sürüm yalnız promotion süresince candidate olarak eklenir. Önceki current sürümler kalıcı matrix'te biriktirilmez; çıkarılmaları unsupported ilanı değildir. OAG 7.26.0 ve Gradle 9.8.1 artık plugin'in previous current değerleridir. Maven POM'larında 7.26.0'ın hâlâ bulunması historical matrix retention değil, o yolların henüz taşınmamış reference girdisidir; canonical Gradle stack 7.27.0/9.9.1 kullanır.

**Ortak felsefe, farklı mekanizma:**

- **Maven:** Consumer `openapi-generator.version` seçer. Parent bu seçimi official `openapi-generator-maven-plugin`, `openapi-generator-core`, `openapi-generator` ve upstream `Java/model.mustache` extraction boyunca property propagation ile coherent tutar. Platform seçilen toolchain'in alignment'ını sahiplenir; concrete version seçimini sahiplenmez.
- **Gradle:** Consumer official `org.openapi.generator` version'ını seçer. Platform `withPlugin` ile running official plugin/runtime'a entegre olur, upstream template'i oradan keşfeder; official version'ı override etmez ve consumer project repository'sinden ikinci bağımsız upstream generator artifact'i resolve etmez. Kendi codegen artifact'ini platform plugin version'ıyla hizalar.
- **Framework/BOM:** Boot supported lines 3.4.x / 3.5.x / 4.x; Springdoc karşılıkları sırasıyla 2.8.x / (2.8.x veya 2.9.x) / 3.x'tir. Exact Boot/Springdoc reference'ları ve internal BOM baseline'ları consumer runtime zorunluluğu değildir. Internal BOM official generator/plugin version authority'si değildir.

Bugünkü Gradle build'inde `minimumOpenApiGeneratorApiCompileVersion=7.25.0` production `compileOnly` API dependency'sini belirler. `currentOpenApiGeneratorVerificationVersion=7.27.0` default functional/E2E runtime'ıdır. `selectedOpenApiGeneratorTestRuntimeVersion`, `-PopenApiGeneratorTestRuntimeVersion=<version>` ile seçilir; verilmezse current kullanılır. Test JVM'ine aktarılır, log'da görünür ve functional test çalışan official runtime artifact'inin version'ını doğrular. Bu test seçimi consumer build'lerine uygulanmaz.

Gradle API için aynı türde ayrı bir minimum compile dependency seçimi yoktur; Gradle API classpath'i build distribution'ından gelir. 9.0.0 compatibility edge'i mevcut TestKit doğrulamasıyla korunur. OAG API compile baseline ve Gradle minimum tested runtime edge'i aynı mekanizma gibi sunulmamalıdır.

#### 7.2 Source version inventory

Generated/ignored `.flattened-pom.xml`, `target`, `build` çıktıları version authority değildir; aşağıdaki tracked source'lar önemlidir.

| Version | Tanımlandığı yerler | Amaç |
|---|---|---|
| Generator 7.26.0 | `openapi-generics-java-codegen/pom.xml` | Generator/core compile/test dependency baseline |
| Generator/Maven plugin 7.26.0 | `openapi-generics-java-codegen-parent/pom.xml` | Official plugin execution, generator/core plugin deps ve upstream template extraction aynı property |
| Generator override 7.26.0 | `samples/spring-boot-{3,4}/customer-service-client/pom.xml`; `samples/type-coverage/{service-response,byoe-response}/client/pom.xml`; `samples/transport-coverage/client/pom.xml`; `samples/generation-coverage/multi-spec-maven-client/pom.xml` | Altı consumer reference pin'i; parent upgrade'ini override etmeye devam edebilir |
| Official Gradle plugin/generator settings API 7.25.0 / verification 7.27.0 | `openapi-generics-gradle-plugin/build.gradle.kts` | Public Gradle plugin API ve upstream config binding compileOnly; current test runtime bağımsız |
| Official Gradle plugin 7.27.0 | `samples/gradle/customer-service/build.gradle.kts` | Canonical stack'in consumer-owned current seçimi; client modülü official plugin ve Generics plugin'ini uygular |
| 7.25.0 / 7.24.0 | `src/functionalTest/.../support/FunctionalTestEnvironment.java` | `MINIMUM_VERIFIED_OFFICIAL_PLUGIN_VERSION` ve `NEGATIVE_BOUNDARY_OFFICIAL_PLUGIN_VERSION`; yalnız integration sınırları |
| 7.25.0 | `src/main/.../integration/OpenApiGeneratorCompatibility.java`, unit expectations | Capability error baseline; reference upgrade ile değiştirilmemeli |
| Gradle 9.9.1 | Plugin ve `samples/gradle/customer-service` wrapper properties/JAR/scripts | İki bağımsız build'in current reference'ı; support minimumu değil |
| Gradle 9.0.0 / 9.9.1 | `OpenApiGenericsGradleVersionCompatibilityFunctionalTest` | Minimum edge ve tek current runtime; previous current kalıcı matrix'ten çıkarıldı |
| Boot 3.5.16 | Server starter `spring-boot.version`; Boot 3 producer/consumer parent'ları ve client BOM property; type-coverage producer/consumer/ilgili contract parent'ları ve client BOM property; transport producer/consumer/contract parent'ları ve client BOM property; multi-spec client BOM | Published starter compile/test baseline ile consumer runtime/dependency-management baseline'ları ayrı anlam taşır |
| Boot 4.1.2 | Maven Boot 4 producer/consumer parent'ları ve client BOM; canonical Gradle stack Boot plugin ve producer/client/consumer dependency BOM girdileri | Mevcut platform reference; yeni Gradle stack bir framework promotion işi değil |
| springdoc 2.9.1 | Platform BOM `springdoc.version`; Boot 3 producer/consumer; type-coverage iki producer; transport producer explicit UI version'ları | BOM common artifact ile sample UI starter pin'leri aynı amaçta değildir |
| springdoc 3.1.1 / 3.1.0 | Maven Boot 4 producer / consumer POM; canonical Gradle producer 3.1.1 | Maven reference farkı: producer 3.1.1, consumer 3.1.0; Gradle consumer projection dependency'si almaz |
| Project 1.3.0-SNAPSHOT | Root project version + `openapi-generics.version`; beş Maven modül parent version'ı; plugin `version`; sample aggregator/artifact/parent/shared-contract-client-starter version property'leri; Gradle sample project/platformVersion | Maven reactor, independently published plugin ve fixture provisioning hizalanması; tek bir root property hepsini otomatik değiştirmez |

E2E runner'ları selected test runtime'ını fixture'lara aktarır; `settings.gradle.kts` official/plugin version'larını property ile alır, sabit current literal'ı taşımaz. Maven plugin version'ını BOM'a taşımak veya her tekrar eden version'ı tek global değere indirgemek bu upgrade'in gereği değildir. Standalone consumer sample pin'lerinin öğretici amacı vardır. Canonical Gradle full-stack sample OAG 7.27.0, Gradle 9.9.1 ve mevcut Boot 4.1.2 reference'larını kullanır; eski Boot 3 standalone Gradle sample kaldırıldı. Bunlar consumer minimumu değildir.

### 8. Verification strategy ve mevcut kanıt

| Katman | Ne doğruluyor | Sınırı |
|---|---|---|
| Contract unit | Envelope/meta/page/error Java davranışı | Framework compatibility değil |
| Server unit/context | Discovery, async unwrapping, config/container validation, descriptor/schema metadata, exclusion, guard, orchestration, missing dependency auto-config | Starter'ın her Boot patch kombinasyonu için ayrı runtime matrix değil |
| Codegen unit | Wrapper metadata failure cases, BYOC registry/imports, envelope derivation, ignored model filtering ve global import temizliği | Gerçek full client compile yerine geçmez |
| Maven sample unit + Failsafe | Boot 3/4 server/controller ve customer RestClient JSON/error interoperability; MockWebServer ile generated client çağrıları | Bütün örnekler live producer/client/consumer network chain testi değil |
| Type-coverage samples | Snapshot production, generated model/API ve consumer compile; built-in/BYOE/custom container/async yüzeyi | Runtime round-trip her shape için ayrı assert edilmiyor |
| Multi-spec Maven | İki execution ve isolated namespace/output'un compile probe ile kullanılabilmesi | Normalization'ın her custom output'ta çalıştığını ölçmüyor |
| Gradle unit (`test`) | Patch/IO/resource ownership, task selection/source layout, capability contract, formatter/action | Consumer integration veya gerçek codegen değil |
| Gradle functional (`functionalTest`) | Published test plugin + fake codegen overlay JAR; application/classpath/alignment, source registration, selected/mixed/multiple task wiring, actual template prep, formatter provisioning, cache | Fake artifact gerçek java-generics-contract üretmez; dry-run generation başarısı sayılmaz |
| Generator compatibility functional | 7.25 positive wiring; 7.24 negative apply/help; selected runtime artifact version assertion; default current 7.27 | Minimum capability testi full E2E değildir; yeni configFile E2E ayrıca 7.25 runtime ile geçti |
| Gradle version functional | 9.0.0 ve 9.9.1 source registration, compile graph dry-run, cache store/reuse | Her distribution'da transport veya gerçek codegen çalıştırmıyor; current distribution'da ayrıca full check/E2E geçti |
| Real-codegen E2E (`e2eTest`) | Direct/page wrapper inheritance, ignored infrastructure absence, compile; iki custom task/cache reuse; configFile root/normalization/cache invalidation | Model-focused; API/supporting files kapalı; bütün transport/BYOE/container runtime round-trip kapsamı değil |
| Canonical Gradle full-stack sample | 3 acceptance testi: producer/spec semantic equality; normalized/shared generated bindings; gerçek producer → generated RestClient → consumer direct/page HTTP deserialization. Clean build ve cache reuse doğrulandı | Type/transport matrix veya yayınlanmış plugin artifact smoke testi değil |
| CI | Core `clean install`, Maven samples `clean verify`, `git diff --exit-code -- samples` | İncelenen workflow'larda Gradle `check`, `e2eTest` veya Gradle sample build çağrısı yok |

Mevcut Maven snapshot testleri committed spec'i yeniden yazar; bazıları metadata marker içeriklerini de assert eder. Drift'i fail ettiren son CI `git diff` adımıdır; snapshot testinin tek başına başarılı olması drift yok demek değildir. Canonical Gradle producer testi varsayılan koşuda snapshot'ı değiştirmeden semantic equality ile drift'te fail eder; generated candidate build altında kalır. Snapshot güncelleme yalnız explicit `-PupdateOpenApiSnapshot=true` ile yapılır. CI JDK 21 ile core, JDK 25 ile Maven samples çalıştırır; yeni Gradle stack henüz CI'ya bağlanmadı. Published code release 17 hedefler; bu, JDK 17 runtime test matrix'iyle aynı iddia değildir.

`transport-coverage/producer` altındaki `TransportControllerIT` için POM'da Failsafe binding yoktur; default Surefire `*IT` seçmez. Buradaki test dosyasının varlığı CI execution kanıtı değildir. Transport client/consumer compilation vardır; generated client üzerinden multipart/form/binary runtime assertion kapsamı customer client JSON testleri kadar güçlü değildir.

Normalization unit test'i unused import removal, formatting ve idempotence'ı assert eder. F2 E2E'si configFile root'undaki gerçek wrapper'da import temizliği/canonical declaration ile compile ve cache replay'i de doğrular. Bu kapsam bütün generated dosyalar/transport biçimleri için exhaustive source equality iddiası değildir.

#### Tamamlanan doğrulamalar ve release review sonucu

| State / koşu | Sonuç ve scope |
|---|---|
| Maven/core başlangıç baseline'ı | OAG 7.26.0 girdileriyle core clean install: 6/6 proje, 324 unit; JDK 25 Maven samples clean verify: 29/29 modül, 81 Surefire + 60 Failsafe, snapshot drift yok |
| İlk Gradle adoption sample (artık kaldırıldı; historical evidence) | Sample'ın 7.26.0/9.8.1 girdileriyle full generated API compile ve ikinci koşuda configuration-cache reuse başarılı; yeni sample reference promotion kanıtı değil |
| Explicit transport server IT | 3/3 başarılı; generated client transport round-trip veya CI binding kanıtı değil |
| Gradle candidate → current | OAG 7.27.0 functional/real-codegen ve Gradle 9.9.1 check/E2E başarılı; promotion sırasında production compatibility fix gerekmedi. O tarihte 40 unit + 25 functional + 2 E2E geçti |
| Bağımsız release review ve F1/F2 | F1 ordinary extension template kaybı ve F2 configFile sourceFolder/layout ayrışması yeniden üretildi; ikisi de production düzeltmesi + kalıcı regression ile kapatıldı |
| **F1/F2 sonrası final full verification** | `./gradlew check e2eTest --rerun-tasks`: **45 unit + 26 functional + 3 E2E**, failure/error/skip yok; Gradle 9.0.0/9.9.1, minimum/negative OAG, source registration/task wiring/multi-task ve configuration-cache yolları dahil |
| Minimum runtime ek doğrulaması | Yeni configFile compilation/normalization/cache E2E'si selected OAG **7.25.0** ile ayrıca başarılı; API/capability 7.25.0 ve negative 7.24.0 korunuyor |
| Canonical Gradle full-stack acceptance | Boot 4.1.2 / Java 25 / OAG 7.27.0 / Gradle 9.9.1; 3/3 test, failure/error/skip yok. `clean build --no-build-cache --configuration-cache` iki koşuda generation/compile/test execution ile başarılı; configuration cache reuse doğrulandı. Kontrollü snapshot drift probe beklenen failure verdi, snapshot geri alındı |
| Sample sonrası plugin regression | `check e2eTest --rerun-tasks`: 45 unit + 26 functional + 3 E2E, failure/error/skip yok; plugin production behavior bu sample işi için değiştirilmedi |

**Kalıcı review sonuçları:** F1 consumer ordinary template provider'ının korunmasıyla; F2 upstream effective config binding, doğru option precedence, tracked fileContents ve locationOnly root hesabıyla çözüldü. Optional upstream sınıflar yalnız withPlugin boundary'sinde yüklenir. Bu gereksinimler artık future task değil, mevcut regression kapsamıdır.

| Kapatılan bulgu / kalıcı regression | Doğruladığı davranış |
|---|---|
| F1 (High) — `OpenApiGenericsTaskWiringFunctionalTest.preservesOrdinaryExtensionTemplateDuringRealGeneration` | Non-selected ordinary generator consumer extension templateDir'ini korur ve gerçek custom model template'ini kullanır; generics preparation çalışmaz |
| F2 (Medium) — `OpenApiGenericsSourceLayoutTest.resolvesUpstreamConfigFileSourceFolder` / `preservesOfficialOptionPrecedence` | JSON/YAML, top-level/nested config ve upstream option precedence aynı effective root'u üretir |
| F2 — `OpenApiGenericsRealCodegenE2ETest.configFileSourceFolderDrivesCompilationNormalizationAndConfigurationCache` | Generated tipi kullanan consumer compile olur; wrapper normalize edilir; forced ikinci koşu cache reuse, config değişikliği yeni root ve cache yeniden değerlendirmesi sağlar |
| Optional-runtime boundary — mevcut `OpenApiGenericsApplyContractFunctionalTest` | Official plugin yokken integration inert kalır; compileOnly settings sınıfları erken yüklenmez |

**Korunacak güçlü kararlar / false positives:** Shared reconstruction artifact/overlay; consumer-owned official version; minimum compileOnly API ile ayrı verification runtime; manifest üzerinden own-codegen alignment; classloader template owner discovery; custom task'a explicit worker classpath; Java source wiring'in task callback'inden ayrı kurulması ve normalization'ın task output snapshot'ından önce çalışması. Intentional no-op/Optional.empty davranışları ve önceki current'ın matrix'ten çıkarılması bug sayılmaz. Unit/functional/real-codegen ve cache dry-run/actual replay birbirine eşit kanıtlar değildir.

Review'ün eski NOT READY sonucu F1/F2 öncesi state'e aitti; açık blocker listesi olarak taşınmıyor. Gradle tarafındaki bu review işleri tamamlandı ve final verification yeşil. Bu, henüz configure edilmemiş production publication veya kalan Maven/sample release adımlarının tamamlandığı anlamına gelmez. Local testlerin CI tarafından sürekli çalıştırılmaması ve direct guard'ın resolved override kapsamı aşağıda ayrı tutulur.

Test report'ları `target/{surefire,failsafe}-reports` ve plugin `build/test-results/{test,functionalTest,e2eTest}` altında geçici output'tur; ardışık koşular bunları overwrite edebilir. Final full log `/private/tmp/openapi-generics-fixes-final-verification.log`, minimum configFile E2E log'u `/private/tmp/openapi-generics-fixes-minimum-configfile.log` altındadır. Kalıcı kanıt regression test kaynakları ve CI'ya bağlanacak doğrulama yollarıdır; eski review log'larının tamamını plan içinde tutmak gerekmiyor.

### 9. Documentation state

| Kategori | Kanıt / önerilen işlem |
|---|---|
| Implementation ile uyumsuz | `docs/compatibility.md` first-class Gradle'ı out-of-scope gösteriyor, client matrix yalnız Maven. Bu genel doküman hâlâ hizalanmalı; AGENTS.md için aynı stale tespit artık geçerli değil |
| Implementation ile uyumsuz | `docs/adoption/server-side-adoption.md` örneğinde `...contract.response.ServiceResponse` var; gerçek package `...contract.envelope.ServiceResponse` |
| Tamamlandı | AGENTS.md'de Maven-only/Gradle out-of-scope ifadeleri, module map, BYOE client configuration örneği ve canonical/internal extension ayrımı düzeltildi; Compatibility & Version Ownership ilkesi eklendi. Yeniden future task değildir |
| 1.2.1'de kalmış | README release badge/What's New, docs home highlights/navigation, adoption ve modül README dependency örnekleri ağırlıkla 1.2.1. Bunlar released kullanım için bilerek kalmış olabilir; unpublished 1.3.0'ı released gibi yazmadan current/released ayrımı yapılmalı |
| Maven-only anlatım | README quick start/module map, docs home client integration, client adoption ve architecture supported scope Maven odaklı; parent modülünün Maven-specific açıklaması ise doğru |
| Gradle integration eksik | Ana adoption/architecture/compatibility sayfalarında official plugin ownership, extra capability, automatic provisioning, selected/custom task/source registration/normalization/cache ve independent build test yolları eksik; `samples/gradle` dokümanları bunları kısmen anlatıyor |
| Version/baseline eski | README 1.2.1 highlights 7.24.0 tarihsel doğrulamasını anlatıyor; current reference olarak okunmamalı. Compatibility sayfasının 7.26.0 reference'ı Maven yollarıyla hâlâ örtüşüyor; plugin current'ı artık 7.27.0 ve wrapper'ı 9.9.1 olduğundan verification surface'leri açık ayrılmalı. Boot 4.1.2/3.1.1 tablosuna karşı gerçek consumer springdoc 3.1.0. Canonical Gradle sample README ve CHANGELOG Unreleased, tamamlanan 7.27.0/9.9.1 ve Boot 4 stack sonuçlarıyla hizalandı; genel compatibility/adoption sayfalarının surface ayrımı hâlâ Adım 7 kapsamındadır |
| Doğru, korunmalı | Rationale'ın Java authority, projection metadata, protocol boundary, specialization/fork ayrımı, transport ownership ve thin wrapper açıklamaları; BYOE'nin producer-side identity, BYOC'nin client mapping oluşu; 7.x platform support line; custom naming/nested graph sınırları |
| Doğru, korunmalı | CHANGELOG tarihsel 1.2.x kayıtları. Unreleased 1.3.0 Gradle capability ve minimum baseline ayrımı yön olarak doğru; “verified”/“full backward compatibility” ifadeleri gerçek test kapsamıyla sınırlandırılmalı; geçmiş sürüm version'ları topluca replace edilmemeli |

Rationale'daki generated-code post-processing eleştirisi ile platformun deterministic hygiene aşaması farklı şeylerdir: contract semantics'i sonradan değiştirmek ile owned generation task'ında unused import/format normalization aynı sahiplik değildir. Açıklama gerekirse bu fark korunmalı, hygiene capability silinmemeli.

### 10. Important architectural invariants

1. Java contract authority korunur; envelope/container modelleri generated contract'a dönüştürülmez.
2. Contract modülüne Spring/Springdoc/Gradle bağımlılığı eklenmez.
3. Server ve client yalnız yayımlanan OpenAPI protocol üzerinden semantik paylaşır; build integration runtime'a sızmaz.
4. Container identity explicit descriptor/metadata ile korunur; Page'e özel reconstruction branch veya class-name guessing eklenmez.
5. Invalid wrapper metadata sessiz stock-model fallback'i üretmez; fail-fast validation korunur.
6. Ordinary upstream API/model/transport generation sahipliği korunur; specialization fork'a dönüşmez.
7. Supported line, minimum edge, tek current reference, negative boundary ve temporary candidate ayrı rollerdir; consumer concrete version seçimi korunur. Previous current kalıcı matrix'te biriktirilmez; promotion minimum/negative sınırları değiştirmez.
8. Plugin/codegen aynı platform version'ını kullanır; upstream plugin/runtime/template version ownership ayrıca korunur.
9. Source registration/custom tasks ve normalization configuration-cache/Gradle mutation modelini bozmamalıdır.
10. Snapshot drift CI'da görünür ve review edilebilir kalır; upgrade başarısı için kör snapshot acceptance yapılmaz.
11. Maven reactor artifact release'i ile Gradle plugin publication ayrı build boundary'leridir; aynı repository veya platform release version'ı bunları tek lifecycle yapmaz.

Fail-fast ilkesini implementation'ın her noktasının mutlak garantisi olarak sunmamak gerekir: external registry invalid BYOC değerlerini warn+skip eder, unsupported introspection shape'leri `Optional.empty` dönebilir, bazı enrichment çözümleri null/no-op ile guard'a bırakılır. Bunlar mevcut davranıştır; bu upgrade'de sessizce hardening yapılarak geriye dönük davranış değiştirilmemelidir.

### 11. Publication architecture — mevcut durum ve intended release modeli

Bu bölüm kaldırılan publication notunun geçerli kararlarını içerir. **Dual-channel production publication henüz uygulanmış veya doğrulanmış değildir.** Local marker/implementation publication başarısı public release başarısı değildir.

| Yüzey | Mevcut implementation | Planned/intended release architecture |
|---|---|---|
| Maven reactor | Contract, BOM, server starter, codegen ve Maven parent modülleri; root `central-publish` profilinde GPG signing ve Sonatype Central Publisher, `autoPublish=false` | Mevcut Maven artifact release boundary'si korunur; Maven Central yayını kendi lifecycle'ından yürür |
| Gradle plugin build | Maven reactor dışında `java-gradle-plugin` + `maven-publish`; plugin identity ve manifest version tanımlı | Plugin artifact/metadata/signing/publication Gradle build'in sorumluluğunda kalır; Maven `<modules>` listesine eklenmez |
| Gradle local publication | `pluginMaven` implementation ve otomatik plugin marker, yalnız `functionalTest` adlı build-local Maven repository'sine publish ediliyor | Implementation artifact Maven Central'a; plugin identity/discovery Plugin Portal üzerinden `plugins {}` tüketimine açılacak |
| Gradle production release wiring | Central release publisher/repository, Portal publisher, signing ve explicit release sources/Javadoc ayarları henüz yok | İki kanalın artifact/metadata/signature akışı release konfigürasyonunda hazırlanıp doğrulanacak; mevcut local test publish task'ı production publish sayılmayacak |
| Formatting | Generated Java için embedded Spotless/Cleanthat/GJF normalization var; plugin build'inde Spotless Gradle source-quality plugin/task'ı yok | Handwritten source quality, generated-source normalization ve publication üç ayrı concern olarak kalır; yayın hazırlığı otomatik yeni formatter feature'ı açmaz |

**Artifact identity ve discovery:** Plugin ID `io.github.blueprint-platform.openapi-generics`; implementation GAV `io.github.blueprint-platform:openapi-generics-gradle-plugin:<platform-release>`; marker GAV `io.github.blueprint-platform.openapi-generics:io.github.blueprint-platform.openapi-generics.gradle.plugin:<platform-release>`. Marker implementation koordinatına bağlanır. Mevcut java-gradle-plugin/maven-publish mekanizması bunu otomatik üretir; elle ikinci marker protokolü tasarlanmaz.

**Central + Portal hedefinin doğru okunması:** Central implementation artifact'inin standart repository/proxy/dependency metadata dağıtımını; Portal plugin identity ve discovery deneyimini sağlar. Ancak Portal yalnız Central'a yönlenen bir katalog kaydı varsayılmamalı: standart publisher main ve marker Maven publication'larını Portal'a yükler. İki kanaldaki aynı implementation GAV/version farklı içerikler taşımamalı. Central'a yalnız implementation koymak da kendiliğinden default `plugins {}` resolution sağlamaz; marker/repository resolution ayrı konudur. Bu ayrım [resmi Portal publication açıklamasında](https://plugins.gradle.org/docs/publish-plugin) da main/marker publication modeliyle belirtilir.

**Hedef consumer deneyimi ve ownership:** Consumer platform plugin'ini ID/version ile seçer; internal marker koordinatını, formatter engine'ini veya codegen provisioning detaylarını yönetmek zorunda kalmaz. Bununla birlikte consumer official `org.openapi.generator` version'ını seçmeye, kendi spec/library/options'ını vermeye ve contract/BYOE/BYOC Java bağımlılıkları ile gerekli project repository'lerini sağlamaya devam eder. Platform plugin'i tek başına apply etmek official plugin'i apply etmez. Plugin discovery repository'si ile codegen/shared-contract dependency resolution repository'si farklı yüzeylerdir. Snapshot development sample'daki `includeBuild`/`mavenLocal`, published consumer kullanımının zorunlu parçası değildir.

**Release version coherence:** Marker, implementation, manifest `Implementation-Version` ve provision edilen platform codegen version'ı aynı platform release'e ait olmalı. Bu, consumer'ın official OAG version'ını platform release'e eşitlemek değildir. Maven ve Gradle ayrı build boundary'lerinde aynı platform release'i koordine eder; mevcut minimum/current/negative compatibility rolleri veya wrapper bundan dolayı değişmez.

**Dependency ordering ve public-availability gate:** `OpenApiGenericsCodegenClasspath` plugin manifest version'ıyla aynı `openapi-generics-java-codegen` koordinatını ekler. Bu dependency plugin'in yayın POM'unda sabit bir runtime dependency olarak görünmese de consumer generation lifecycle'ının gerçek dependency'sidir. Apply/help sırasında declaration oluşturulması, artifact'in resolve edildiği veya generator'ın çalıştığı anlamına gelmez.

Public plugin version'ı tüketilebilir hale gelmeden önce, hedef external dependency repository'sinde şunlar doğrulanmalı:

- Aynı platform release version'ında `openapi-generics-java-codegen` **JAR + published POM** ve gerekli runtime/transitive graph.
- Mevcut codegen POM'undaki compile dependency nedeniyle aynı-version `openapi-generics-contract`; generated client'ın compile dependency'si de consumer tarafından ayrıca sağlanır.
- Gerçek published POM'un referans verdiği parent/BOM/metadata varsa bunların resolution'ı. Şu anki flattened POM doğrudan version'lı dependency'ler içerip parent/BOM referanslarını kaldırıyor; source POM'a bakarak gereksiz public dependency varsayılmamalı, release POM incelenmeli.

Codegen POM'undaki upstream generator/core `provided` dependency baseline'ı consumer'ın official runtime version'ını belirleyen yeni bir gate değildir. Official runtime consumer-owned kalır. Server starter veya Maven codegen parent gibi plugin generation graph'ında gerekmeyen artifact'lerin publication'ı, sırf aynı repository'de oldukları için plugin'in teknik dependency gate'ine eklenmez; koordineli platform release planı onları ayrıca kapsar.

```text
release bundles build/sign/private stage (iki build paralel hazırlanabilir)
        ↓
gerekli Maven release artifact'leri external repository'de public availability
        ↓
Gate A: staged plugin + external-only codegen/contract graph ile generation/compile
        ↓
Gradle implementation Central public release + Portal main/marker publication
        ↓
Gate B: tamamen published repository'lerden consumer smoke
        ↓
release tamamlandı ilanı
```

**Gate A — plugin public publication öncesi:** Publisher upload success/approval mesajı veya local cache yeterli kanıt değildir. İzole consumer/cache ile external repository'den aynı-version codegen/contract graph resolve edilir; otomatik provisioning kullanılarak selected GenerateTask, template preparation ve gerçek generation/compile çalıştırılır. Plugin henüz public olmadığı için test edilen **plugin bundle** private/local staging'den gelebilir; codegen/contract resolution'ı ise mavenLocal, composite substitution, isolated E2E codegen repository'si veya manuel dependency override ile karşılanmamalı. Bu şekilde plugin'i erkenden public yapmadan gate ölçülebilir. Salt `help`, dependency declaration veya index/search görünürlüğü gate'i geçirmez.

**Gate B — iki public kanal sonrası:** Marker → implementation discovery, manifest/codegen version alignment ve generation/compile tümü yayımlanan repository'lerden, mavenLocal/includeBuild olmadan tekrar doğrulanır. Aynı implementation GAV/version iki kanalda farklı içerik taşımamalı. Kanal erişilebilirliği/Portal approval tek atomic işlem değildir; kısmi publication ile release tamamlandı denmez.

Dependency artifact'lerinin private staging'den önce public olması gerekmez; **public plugin publication'dan önce Gate A'nın başarılı olması gerekir**. Önerilen sequencing, gerekli Maven artifact'lerini önce public release edip actual resolution'ı doğrulamak, sonra plugin'i iki kanalda açmaktır. Gate A başarısızsa public plugin publication bekler; eski codegen version'ına fallback yapılmaz ve consumer official OAG version'ı değiştirilerek sorun gizlenmez. Bu gate'ler planlanmıştır; bu doküman güncellemesinde external release resolution veya publication çalıştırılmadı.

**Wrapper ve kalite sınırı:** Wrapper repository build reproducibility aracıdır; publication endpoint'i veya consumer minimumu değildir. Generated-source normalization consumer GenerateTask lifecycle'ına; handwritten source quality repository verification'a; signing/metadata/upload ise publication lifecycle'ına aittir. Biri diğerinin tamamlandığına kanıt olmaz.

Resmi Portal workflow'u ayrıca publisher konfigürasyonu ve publication validation gerektirir; mevcut build'de henüz `publishPlugins` yolu yoktur. Gerekli workflow hazır olduğunda upload yapmayan validation, metadata/signature ve release-consumer smoke kontrolleri release adımlarında değerlendirilir. [Resmi Gradle publishing rehberi](https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html) bu validation/upload ayrımını tanımlar. Bu cleanup'ta publisher seçimi/version bump veya publication uygulanmadı.

## Bölüm B — Proposed Upgrade Plan

### 1. Kalıcı upgrade lifecycle ve promotion kriteri

```text
new upstream release
        ↓
temporary candidate verification (current değiştirilmeden)
        ↓
pass → promote to current reference
fail → investigate concrete compatibility break
        ↓
minimum / negative boundaries remain unchanged
unless the actual compatibility boundary changes
```

Candidate testi, consumer version ownership'ını değiştiren bir seçim mekanizması değildir; repository'nin kendi integration compatibility doğrulamasıdır. Candidate başarısız olursa failure mevcut integration sınırında değerlendirilir; yalnız somut failure'ın gerektirdiği en küçük düzeltme yapılır. Yeni feature, genel hardening, unrelated refactor veya consumer framework pin'leri bu akışın parçası değildir. Current promotion ancak ilgili verification surface'inin başarılı sonuçlarından sonra yapılır.

| Surface | Kalıcı roller | Geçici rol |
|---|---|---|
| Gradle plugin / official OAG integration | API/capability minimum **7.25.0** + current **7.27.0** + negative **7.24.0** | Yeni OAG release çıktığında selected runtime override ile candidate |
| Gradle runtime | Supported **9.x**; minimum tested edge **9.0.0** + current/reference wrapper **9.9.1** | Yeni 9.x distribution ile wrapper değiştirilmeden candidate |
| Maven generation | Supported OAG **7.x**; consumer-selected coherent toolchain ve repository'nin kendi current reference girdisi | Parent/reference promotion öncesi property override ile candidate |

Kalıcı matrix'te previous current OAG 7.26.0 veya Gradle 9.8.1 tutulmaz. Gerekli görülürse upgrade sırasında geçici karşılaştırma koşusu yapılabilir; bu ek support boundary değildir. Yeni candidate başarılı olunca current'ın yerini alır. Minimum ve negative sabit kalır; başarısız candidate bu sınırların otomatik yükseltilmesini haklı çıkarmaz.

Örneğin ileride OAG 7.28.0 çıktığında minimum 7.25.0, current 7.27.0 ve negative 7.24.0 korunurken 7.28.0 candidate çalıştırılır. Başarılıysa current 7.28.0 olur; 7.27.0 historical result olarak kalır, kalıcı matrix noktası olmaz. Gradle 9.x için aynı süreç 9.0.0 edge'i korunarak uygulanır.

Test kapsamı açık tutulmalıdır: explicit OAG minimum testi integration wiring/capability kapsamındadır; Gradle version testi source registration ve cache/task graph kapsamındadır. Current üzerinde ayrıca gerçek generation/compile ve multi-task cache E2E vardır. Bu yapı her desteklenen patch veya bütün kombinasyonlar için full Cartesian E2E iddiası taşımaz. Current wrapper, minimum Gradle API'ye ayrı compile edildiği anlamına da gelmez.

### 2. Tamamlanan işler — future work değil

| İş | Güncel sonuç |
|---|---|
| Repository/core/sample başlangıç doğrulaması | A.8'deki mevcut baseline kanıtları toplandı; codegen/Maven sample reference'ları o koşularda 7.26.0 idi |
| AGENTS.md alignment | Version ownership, supported/reference ayrımı, Gradle lifecycle, BYOE/protocol bilgisi hizalandı |
| Gradle plugin version-role ayrımı | Production minimum API compileOnly 7.25.0; ayrı current verification ve selected test runtime property; functional/E2E aktarımı ve runtime görünürlüğü tamamlandı |
| OAG 7.27.0 candidate → current | Mevcut production implementation ile functional + real-codegen E2E geçti; default reference 7.27.0'a taşındı |
| Gradle 9.9.1 candidate → current | Önce candidate distribution ile full check/E2E geçti; sonra plugin wrapper/current compatibility noktası 9.9.1'e taşındı |
| Minimum / negative koruması | OAG 7.25.0 compile/API ve positive capability, 7.24.0 negative case; Gradle 9.0.0 edge korundu |
| Bağımsız Gradle release review / F1-F2 | İki concrete consumer bug düzeltildi; ordinary template retention ve configFile effective sourceFolder kalıcı regression'a dönüştü |
| Final Gradle verification | 45 unit + 26 functional + 3 E2E yeşil; yeni configFile E2E minimum 7.25.0 runtime'ında da geçti |
| Canonical Gradle-first full-stack adoption (Adım 4) | `samples/gradle/customer-service`: contract/producer/client/consumer; direct/page gerçek HTTP ve snapshot drift dahil 3 test; clean build + cache reuse tamamlandı; redundant standalone client kaldırıldı |
| Boot 4.1.2 reference hedefi | Üç Boot 4 sample POM'u zaten 4.1.2; tekrar Boot version değişikliği gerekmiyor |

Gradle version promotion'ı compatibility fix gerektirmedi; bunu izleyen bağımsız review F1/F2 için dar production düzeltmeleri gerektirdi ve bunlar tamamlandı. Consumer ownership ve shared reconstruction modeli korundu. Review/F1/F2 veya tamamlanmış plugin reference upgrade'i kalan adımların önüne yeniden konulmaz.

### 3. Kalan platform upgrade kapsamı ve targeted riskler

Gradle plugin ve canonical Gradle stack current reference'ları doğrulandı; Maven/core reference'larının da taşındığı varsayılmamalıdır:

- `openapi-generics-java-codegen` ve Maven parent reference'ları hâlâ **7.26.0**.
- Altı Maven client sample kendi **7.26.0** override'larını taşıyor.
- Canonical Gradle stack OAG **7.27.0**, wrapper **9.9.1**, Boot **4.1.2** / Java **25** ile tamamlandı; eski standalone sample redundant olduğu için kaldırıldı.
- Kalan reference promotion işi Maven/core ve altı Maven client sample içindir; consumer'ların supported line içindeki concrete version seçimi korunur.
- Boot 4.1.2 hedefi tamamlanmış; consumer Springdoc 3.1.0 / producer 3.1.1 farkı ayrı ve non-blocking reference alignment kararıdır.

Önceki upstream incelemesinden kalan, bu repository için doğrudan ilgili kanıtlar:

| Yüzey | 7.26 → 7.27 bulgusu | Kalan Maven/sample doğrulamasındaki anlamı |
|---|---|---|
| `Java/model.mustache` | İçerik aynı; opening/closing patch anchor'ları korunmuş | Gradle gerçek artifact hazırlığı geçti; Maven'in ayrı extraction/Ant patch lifecycle'ı da temiz output ile doğrulanmalı |
| Official Gradle plugin / GenerateTask | İncelenen tagged kaynaklar aynı; candidate koşuları başarılı | Plugin için yeni production fix veya ek API kullanımı gerekmiyor |
| Java/shared model processing ve enum template'leri | Enum/Serializable, `$ref`+allOf, discriminator helper, numeric bound ve OAS 3.1 map işleme değişiklikleri | Mevcut codegen unit, type coverage, filtering/import cleanup ve generated model compilation sonuçları değerlendirilir |
| RestClient API template | Body local kaldırma; required/optional header ve form emission farkları | Model-focused Gradle E2E yerine full API sample compilation ve mevcut client interoperability testleri kendi kapsamlarında kullanılmalı |
| RestClient helper / generated build metadata | İncelenen ApiClient template aynı; generated POM dependency default'ları farklı | Generated standalone POM değişimi consumer Boot BOM'unu elle yükseltme gerekçesi değildir |

Bu targeted inceleme exhaustive upstream audit değildir; yeni investigation görevi olarak büyütülmemelidir. Server projection veya contract tasarımını generator release nedeniyle değiştirmek için bugün somut failure yoktur.

Kaynaklar: [OAG 7.27.0 release](https://github.com/OpenAPITools/openapi-generator/releases/tag/v7.27.0), [7.26–7.27 tagged diff](https://github.com/OpenAPITools/openapi-generator/compare/v7.26.0...v7.27.0), [Java model template](https://github.com/OpenAPITools/openapi-generator/blob/v7.27.0/modules/openapi-generator/src/main/resources/Java/model.mustache), [RestClient API template](https://github.com/OpenAPITools/openapi-generator/blob/v7.27.0/modules/openapi-generator/src/main/resources/Java/libraries/restclient/api.mustache), [Gradle 9.9.1 release notes](https://docs.gradle.org/9.9.1/release-notes.html). Bunlar önceki incelemenin kaynaklarıdır; bu doküman görevinde yeniden upstream araştırması yapılmadı.

### 4. Kalan execution sırası

Her adım ayrı scope'tur. Önce ilgili candidate koşusu yapılır; başarısızlıkta yalnız somut compatibility break ele alınır. Aşağıdaki plan bu görevde kod/build değişikliği yetkisi veya yapılmış iş iddiası değildir.

#### Adım 1 — Maven generation yolunu 7.27.0 candidate ile doğrula

- **Amaç:** Gradle'da tamamlanan promotion'ın Maven lifecycle'ını otomatik doğruladığı varsayımını önlemek; current POM değerlerini değiştirmeden Maven-specific candidate kanıtı almak.
- **Değişmesi muhtemel dosyalar:** Candidate koşusu için tracked değişiklik gerekmez; `-Dopenapi-generator.version=7.27.0` verification girdisi kullanılır.
- **Korunacak davranış:** Consumer-selected version'ın official Maven plugin/core/generator/template extraction boyunca coherence'ı; BYOC/BYOE, ignored models ve contract authority.
- **Risk:** Eski effective template output'u veya stale installed snapshot artifact'inin gerçek seçimi gizlemesi; Maven Ant patch lifecycle'ının Gradle'dan farklı olması.
- **Doğrulama:** Temiz codegen test/build ve candidate ile representative Maven client generation/compile; resolved plugin/core/generator/extracted template sürümü, wrapper inheritance ve ignored-model/import çıktıları kontrol edilir. Maven-written source baseline ve running Gradle runtime ayrı roller olarak kalır.
- **Geçiş kriteri:** Candidate Maven yolunda başarılıdır; açıklanamayan model/source farkı yoktur. Failure varsa en küçük gerekli fix ayrı review edilir; reference erken değiştirilmez.

#### Adım 2 — Maven codegen ve parent current reference'ını promote et

- **Amaç:** Başarılı Maven candidate sonrası repository'nin codegen dependency ve Maven parent default'unu 7.27.0 yapmak.
- **Değişmesi muhtemel dosyalar:** `openapi-generics-java-codegen/pom.xml`, `openapi-generics-java-codegen-parent/pom.xml`.
- **Korunacak davranış:** Parent property propagation; general OAG 7.x support policy; platform project version ve contract reconstruction semantics. Gradle plugin API compile baseline 7.25.0 kalır; Maven codegen dependency baseline'ıyla aynı role sokulmaz.
- **Risk:** Superclass model graph farkları; temiz generation dışında görülemeyen template reuse; default değer değişiminin consumer requirement gibi yorumlanması.
- **Doğrulama:** Temiz core install/test ve representative Maven generation; yeni codegen artifact'iyle Gradle plugin'in mevcut current/minimum/negative verification yolları tekrar değerlendirilir. Codegen artifact değişikliği, Gradle'da bugün doğrulanan artifact girdisinden farklıdır.
- **Geçiş kriteri:** Yeni artifact'lerle ilgili kontroller geçer; metadata/ignore/import davranışı korunur. Somut patch ihtiyacı yoksa template/integration koduna dokunulmaz. Previous current 7.26.0 için kalıcı matrix eklenmez.

#### Adım 3 — Maven sample reference girdilerini ilerlet

- **Amaç:** Parent promotion'ını override ederek eski reference'ta kalan altı standalone Maven client örneğini 7.27.0 ile hizalamak.
- **Değişmesi muhtemel dosyalar:** A.7.2'de listelenen altı Maven client POM'u; yalnız açıklanmış gerçek projection değişikliği varsa ilgili spec snapshot'ı.
- **Korunacak davranış:** BYOC/BYOE, scalar/list/set/page/custom-container ve async yüzeyi; isolated multi-spec output; Boot 3/4 consumer BOM ownership'ı.
- **Risk:** Ordinary generated API/model diff'leri, RestClient header/form emission değişimi; generator release nedeniyle ilgisiz framework version churn.
- **Doğrulama:** Güncel core install ardından JDK 25 sample clean verify; customer client unit/Failsafe JSON/error interoperability, type/multi-spec compile probe ve snapshot diff review. Mevcut transport testlerinin execution/client-round-trip sınırları A.8'deki gibi raporlanır.
- **Geçiş kriteri:** Reactor başarılı; tracked source/spec farkları gerekçeli; duplicate shared contract generation yok. Yeni test feature'ları otomatik önkoşul yapılmaz.

#### Adım 4 — Canonical Gradle-first Boot 4 full-stack adoption — TAMAMLANDI

- **Implementation:** `samples/gradle/customer-service` altında tek wrapper/settings ile `contract`, `producer`, `client`, `consumer` modülleri kuruldu. Boot **4.1.2**, Java **25**, official OAG **7.27.0**, Gradle **9.9.1** reference'ları kullanılır. Boot 4.1.2 mevcut platform reference'ıdır; Maven promotion veya framework upgrade yapılmadı.
- **Contract flow:** Pure Java shared/BYOC `CustomerDto`; starter ile producer projection → committed `spec/customer-api-docs.json` → custom `GenerateTask` ile generated RestClient → consumer gateway. Direct `ServiceResponse<CustomerDto>` ve paged `ServiceResponse<Page<CustomerDto>>` normal application return type'larıdır. Client build live producer'a bağımlı değildir.
- **Ownership/lifecycle:** Consumer official plugin ve normal generator seçeneklerini seçer (`useSpringBoot4` / `useJackson3` dahil). Generics plugin codegen/templates, non-default `src/gen/java` root registration, compile dependency ve normalization sağlar. Manuel sourceSets/compileJava.dependsOn, generator classpath veya templateDir workaround eklenmedi. Plugin production behavior değiştirilmedi.
- **Eski sample kararı:** Standalone `samples/gradle/customer-service-client` custom task, BYOC, full API compile ve cache adoption değerini yeni `client` modülü tamamen taşıdığı için kaldırıldı. Yeni consumer compile/runtime testleri eski compile probe'un değerini de taşır; yalnız yeni sample oluştu diye otomatik silme yapılmadı.
- **Kanıt:** `OpenApiSnapshotTest` producer spec'ini generated build çıktısına alıp committed snapshot ile semantik karşılaştırır; kontrollü drift değişikliği beklenen test failure verdi ve geri alındı. `GeneratedContractTest` automatic compilation, shared model duplication absence ve wrapper normalization/import temizliğini doğrular. `CustomerHttpTest` random-port gerçek Boot producer ve ayrı consumer context'inde generated RestClient ile direct/page HTTP çağrıları yapar; shared DTO exact type, ServiceResponse inheritance, Page exact type, meta ve pagination değerlerini assert eder. Mock HTTP veya elle JSON parse kullanılmaz.
- **Verification sonucu:** Üç acceptance testi yeşil. İki `clean build --no-build-cache --configuration-cache` koşusunda gerçek generation/compile/test execution başarılı; configuration cache reuse doğrulandı. Plugin `check e2eTest --rerun-tasks`: **45 unit + 26 functional + 3 E2E**, failure/error/skip yok. Generated/build çıktıları commit edilmez; yalnız OpenAPI snapshot committed kaynaktır.
- **Sınır / kalan işler:** BYOE, custom containers, List/Set/async ve transport/multi-spec/mixed-generator coverage genişletilmedi. CI entegrasyonu Adım 5, published artifact smoke/publication Adım 6/9 kapsamındadır; bu sonuçlar bunları yapılmış göstermez.

#### Adım 5 — CI yürütmesini mevcut verification rolleriyle hizala

- **Amaç:** Bugün local olarak doğrulanan Gradle scope'unu sürdürülen CI yolunda görünür kılmak; ekstra ürün davranışı eklememek.
- **Değişmesi muhtemel dosyalar:** `.github/workflows/build.yml` veya ayrı Gradle verification workflow.
- **Korunacak davranış:** Mevcut Maven core-before-sample sırası, JDK 21/25 ayrımı, spec drift failure; Gradle `check` ve explicit `e2eTest` ayrımı.
- **Risk:** Gereksiz Cartesian/history matrix maliyeti, stale artifact reuse ve test varlığını CI execution saymak.
- **Doğrulama:** Current ile check/E2E; existing explicit minimum/negative OAG case'leri ve 9.0.0/current Gradle edge'leri görünür yürütülür. Yeni upstream sürümde geçici candidate override kullanılabilir; previous current matrix'e eklenmez. Tamamlanmış Adım 4 canonical Gradle stack için producer/spec drift, generated client + consumer build, gerçek HTTP/deserialization ve configuration-cache reuse yolu CI'da yürütülür; type/transport matrix tekrar edilmez. Hangi katmanın wiring, compilation veya runtime interoperability kanıtladığı raporlanır.
- **Geçiş kriteri:** Fresh CI koşusu ilgili kontrolleri gerçekten yürütür; minimum/current/negative ayrımı ve snapshot drift failure korunur. Daha geniş regression kapsamı bu adımın önkoşulu değildir.

#### Adım 6 — Planlanan Gradle production publication yolunu hazırla

- **Amaç:** A.11'deki intended Central + Plugin Portal release modelini, mevcut local test publication'dan ayrı bir release yolu olarak hazırlamak. Bu iş mevcut implementation olarak raporlanmaz.
- **Değişmesi muhtemel dosyalar:** Yalnız ileride onaylanmış release görevinde plugin build/publication ayarları ve ilgili release workflow; Maven reactor'a Gradle modülü eklenmez.
- **Korunacak davranış:** Otomatik marker → implementation identity, manifest/codegen platform-version alignment, consumer-owned official runtime; generated normalization ve handwritten quality concern'lerinin ayrılığı.
- **Risk:** Local TestKit publication'ını public release sanmak; Portal'ı metadata-only kabul etmek; aynı version'a iki farklı artifact üretmek; henüz Central'da olmayan codegen version'ını Portal plugin'inin provision etmesi.
- **Doğrulama:** Implementation/marker POM ve Gradle metadata, uygun sources/Javadoc artifact'leri, signing, repository hedefleri ve gerekli public metadata release bundle üzerinde incelenir. Minimum/current consumer compatibility ile publisher'ın build gereksinimleri birbirine karıştırılmaz; dry validation upload yerine geçmez.
- **Geçiş kriteri:** İki kanal için review edilebilir artifact/metadata set'i, ayrı private staging/public publication aşamaları ve A.11 Gate A/B sırası vardır; test-only publication yolu bozulmamış; henüz publish edilmiş iddiası yapılmıyor. Bu cleanup'ta ilgili implementation başlatılmaz.

#### Adım 7 — Genel dokümanları actual state ve release state ile hizala

- **Amaç:** A.9'daki hâlâ açık stale ifadeleri ve farklı verification surface'lerinin exact reference değerlerini doğru anlatmak.
- **Değişmesi muhtemel dosyalar:** README, docs home, architecture/client/server adoption/compatibility, ilgili module/sample README'leri ve yalnız CHANGELOG Unreleased. AGENTS.md alignment zaten tamamlandı.
- **Korunacak davranış:** Java authority, specialization/upstream ownership; supported line ile current/reference ayrımı; consumer version sovereignty; tarihsel 1.2.x kayıtları.
- **Risk:** Unreleased 1.3.0'ı published göstermek; bütün platform reference'larını aynı anda taşınmış gibi yazmak; 7.24'ü global unsupported veya current wrapper'ı minimum ilan etmek.
- **Doğrulama:** Her “verified” iddiası gerçekten başarılı surface/koşuya bağlanır. Maven ve Gradle aynı coherence felsefesini farklı mekanizmalarla anlatır. Boot/Springdoc/internal BOM exact değerleri consumer pin'i gibi sunulmaz; tamamlanmış plugin promotion'ı future task değildir. Adım 4 canonical Gradle-first full-stack implementation/verification tamamlandı; sample README ve Unreleased kaydı güncellendi. Genel adoption sayfalarının kalan hizalaması bu adımdadır; redundant standalone sample artık kaldırılmıştır.
- **Geçiş kriteri:** Docs/code/test/CI kapsamı tutarlı; runtime verification ile planned/public publication state ayrılmış, F1/F2 tamamlandı olarak anlatılmış; history matrix veya yeni consumer version zorunluluğu icat edilmemiş.

#### Adım 8 — Combined regression ve release artifact review

- **Amaç:** Kalan Maven/sample ve planned publication hazırlığını aynı release candidate artifact set'i üzerinde doğrulamak; tamamlanan Gradle review/F1/F2 işlerini tekrar açmamak.
- **Değişmesi muhtemel dosyalar:** Yeni implementation gerekmemeli; yalnız gerekçelendirilmiş spec/release kayıtları.
- **Korunacak davranış:** A.10 invariant'ları; OAG 7.x / Gradle 9.x supported lines; sabit minimum/negative edge'ler ve consumer-owned concrete versions.
- **Risk:** Yeni Maven-built codegen artifact'i ile Gradle worker/template runtime resolution'ın birleşik etkisi; unexplained snapshot acceptance.
- **Doğrulama:** Release version/manifest/marker/codegen coherence; uygun packaging'e göre sources/Javadoc/signature/metadata kontrolleri; temiz core install ve JDK 25 Maven samples verify; Gradle check + explicit E2E ve F1/F2/cache regression'ları; canonical Gradle full-stack sample producer/spec/client/consumer build, gerçek HTTP + deserialization, snapshot drift ve configuration-cache reuse doğrulaması ile tracked diff review. Yalnız kalıcı minimum/current/negative rolleri kullanılır. Doğrulanan bundle upload öncesi sabitlenir; publisher task'larının E2E'yi kendiliğinden çalıştırdığı varsayılmaz.
- **Geçiş kriteri:** İlgili kontroller yeşil; artifact/metadata aynı release set'ine ait; açıklanamayan behavioral/source/spec farkı yok. Gradle current ve F1/F2 doğrulaması zaten tamamlanmış; bu gate daha geniş platform/sample/publication hazırlığı içindir, public upload success değildir.

#### Adım 9 — Ayrı publication kanallarını tamamla ve published-consumer smoke yap

- **Amaç:** Doğrulanmış release set'ini Maven ve Gradle'ın kendi publication lifecycle'larıyla dağıtmak; repository-local bootstrap'tan bağımsız gerçek tüketimi doğrulamak.
- **Değişmesi muhtemel dosyalar:** Yeni runtime implementation gerekmemeli; release workflow/kayıtları ve gerçek availability sonrası release açıklamaları. Bu adım mevcut cleanup görevinin execution scope'u değildir.
- **Korunacak davranış:** Maven artifact'leri Maven reactor'dan Central'a; Gradle implementation/marker release'i Gradle build'den A.11'deki iki kanala gider. Same platform release alignment, consumer official-version ownership ve existing compatibility policy korunur.
- **Risk:** Repository indexing/Portal approval gecikmeleri, kısmi kanal başarısı, aynı version'a farklı içerikle tekrar publication; development repositories'nin eksik public artifact'i gizlemesi.
- **Doğrulama:** Önce gerekli same-version codegen/contract published dependency graph'ı external repository'de public olur; **Gate A** staged plugin ile external-only dependency resolution + gerçek generation/compile'ı doğrular. Yalnız bu gate sonrası Gradle implementation Central ve Portal main/marker public publication yapılır. Sonra **Gate B** tamamen public repository'lerden plugin DSL discovery ve consumer generation/compile'ı doğrular. Diğer platform artifact'leri kendi koordineli Maven release kapsamındadır; teknik plugin graph'ında olmayan artifact'ler gate'e keyfi eklenmez.
- **Geçiş kriteri:** Gate A plugin public publication'dan önce, Gate B iki kanal sonrası başarılıdır; hedef kanallar aynı release identity/artifact set'ini sunar. Ancak bundan sonra public release tamamlandı olarak ilan edilir. Buradaki mimari tercih, upload veya external resolution'ın şu an yapılmış olduğu anlamına gelmez.

### 5. Non-blocking follow-up — ana upgrade yolundan ayrı

Aşağıdakiler gözlemdir; bugünkü başarılı candidate/current promotion'ını geçersiz kılmaz ve remaining upgrade'in otomatik önkoşulu değildir. Somut compatibility failure ortaya çıkarsa yalnız ilgili madde gerekli fix kapsamına alınır.

| Alan | Ayrı değerlendirme | Mevcut kanıt/sınır |
|---|---|---|
| Maven custom output normalization | Default parent include pattern'i dışındaki multi-output root'lar | Gradle F2/root/cache regression tamamlandı; bu ayrı Maven sample hygiene kapsamıdır |
| Maven template patch validation | Gradle patcher'daki exact-once/already-patched kontrolleriyle parity | Mevcut Maven marker guard ayrı implementation; yeni drift failure yokken upgrade bahanesiyle rewrite gerekmez |
| Transport verification | Producer `*IT` lifecycle binding'i veya generated client multipart/form/binary round-trip regression | Explicit server IT 3/3 geçti; CI binding ve client round-trip farklı kapsamdır |
| Resolved dependency overrides | Direct declaration guard dışındaki resolutionStrategy/transitive override senaryoları | Mevcut alignment behavior değiştirilmedi; unrelated hardening değildir diye varsayılmamalı |
| Boot 4 sample Springdoc alignment | Consumer 3.1.0 → producer reference 3.1.1 ile hizalama tercihi | Boot 4.1.2 hedefi tamamlanmış; ayrı patch/reference değişimi, consumer zorunluluğu veya generator upgrade prerequisite'i değil |

Bu planın hiçbir adımı supported line, verified reference, minimum edge veya consumer version ownership rollerini birbirinin yerine kullanmamalıdır.
