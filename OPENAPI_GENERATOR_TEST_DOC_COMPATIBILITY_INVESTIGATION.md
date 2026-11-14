# Generated test/documentation output compatibility investigation

2026-11-14 · İncelenen revision: `f1ec8ea439a6d152230a5f1887e7f42d8e050592`.
OAG **7.27.0**, platform **1.3.0**, `java-generics-contract`, `library=restclient`,
`useSpringBoot3=true`, Jackson 2, `openApiNullable=false`, `useAbstractionForFiles=true`.
Maven 3.9.9 / JDK 21.0.6. Sonuçlar bu kombinasyon içindir; bütün 7.x/library kombinasyonları için iddia değildir.

> Bu investigation aşağıdaki revision’ın pre-fix kanıtını korur. Wrapper model-doc follow-up çalışma ağacında uygulandı; kapsam ve yeni kanıt en sondaki follow-up notundadır.

## 1. Problem / symptom ve sonuç

**Dört flag de seçilen sample'da generation ve gerçek generated-test compilation ile çalıştı.**
Generic reconstruction nedeniyle tekrar üretilebilen bir Java compilation failure bulunmadı.
İki ayrı documentation problemi doğrulandı:

- Suppressed/reused modellere ait Markdown dosyaları üretilmiyor; kalan wrapper dokümanları bu dosyalara link veriyor ve Java generic binding kimliğini açıklamıyor.
- RestClient API dokümanının Java örnekleri yanlış import/bootstrap ve File/Resource varsayımları içeriyor. Aynı sorun stock upstream `java` generator ile de oluşuyor.

“Test sınıfları oluşmuyor” gözlemi netleştirildi: gerçek `transport-coverage/client` altında
**3 API + 5 model test dosyası** vardı; **0 compiled test class** vardı. Dosyalar modülün
`src/test/java` dizinine değil, `target/generated-sources/openapi/src/test/java` altına yazılıyor.
Bu dizin mevcut Maven sample'da test source root'u olarak kayıtlı değil.
Kullanıcı da dosyaların oluştuğunu teyit etti; bu bir generation defect değil.

## 2. Reproduction

Ana fixture: `samples/transport-coverage/client` ve committed `../spec/transport-coverage.yaml`.
Küçük bir Maven generation execution'ı içinde 3 operasyon, 2 wrapper, 3 ordinary DTO ve suppressed
`Meta`/`Sort` var. Production sample/POM değiştirilmedi; bağımsız kopyalar `/private/tmp` altında çalıştırıldı.
Platform 1.3.0 parent/codegen/contract artifact'lerinin yerel Maven deposunda bulunması gerekir
(repository bootstrap: root'ta `mvn clean install`; public publication gerektirmez).

Her koşuda fresh kopya hazırlama, repository root'tan:

```bash
probe="$(mktemp -d)"
mkdir -p "$probe/client" "$probe/spec"
cp samples/transport-coverage/client/pom.xml "$probe/client/pom.xml"
cp -R samples/transport-coverage/client/src "$probe/client/src"
cp samples/transport-coverage/spec/transport-coverage.yaml "$probe/spec/"
# Argümanları tek flag, dört flag veya boş liste olarak değiştir.
python3 - "$probe/client/pom.xml" generateApiDocumentation generateApiTests generateModelDocumentation generateModelTests <<'PY'
from pathlib import Path
import re, sys
p = Path(sys.argv[1])
s = p.read_text()
for flag in ['generateApiDocumentation', 'generateApiTests',
             'generateModelDocumentation', 'generateModelTests']:
    value = str(flag in sys.argv[2:]).lower()
    s = re.sub(fr'<{flag}>.*?</{flag}>', f'<{flag}>{value}</{flag}>', s)
p.write_text(s)
PY
mvn -B -ntp -f "$probe/client/pom.xml" clean test-compile
find "$probe/client/target/generated-sources/openapi/src/test/java" -name '*Test.java'
```

Bu komut tek başına generated testleri derlemez: normal sample log'u `testCompile: No sources to compile`
der. **Compilation kontrolü** için yalnız kopyadaki POM'a şu geçici profili ekle:

```xml
<profiles>
  <profile>
    <id>compile-generated-tests</id>
    <dependencies>
      <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
      </dependency>
    </dependencies>
    <build><plugins><plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>build-helper-maven-plugin</artifactId>
      <executions><execution>
        <id>probe-generated-tests</id>
        <phase>generate-test-sources</phase>
        <goals><goal>add-test-source</goal></goals>
        <configuration><sources>
          <source>${project.build.directory}/generated-sources/openapi/src/test/java</source>
        </sources></configuration>
      </execution></executions>
    </plugin></plugins></build>
  </profile>
</profiles>
```

`mvn -B -ntp -f "$probe/client/pom.xml" clean test-compile -Pcompile-generated-tests`
komutuyla gerçek test compilation doğrulandı. `test` fazı ayrıca scaffolding execution için çalıştırıldı.
JUnit sürümü kopyadaki mevcut Boot BOM'undan gelir; bu profil bir production değişikliği önerisi değildir.

İki önemli kontrol:

- JUnit dependency kaldırılıp test root'u kayıtlı bırakılırsa `package org.junit.jupiter.api does not exist` oluşuyor. Bu consumer test classpath prerequisite'i; wrapper reconstruction kusuru değil.
- POM'daki literal `false` dururken yalnız `-Dopenapi.generator.maven.plugin.generateApiTests=true`
  ve `...generateModelTests=true` verilmesi bu fixture'da yeterli olmadı. `-X` çıktısı her iki effective
  alanı da `false` gösterdi; 0 test dosyası oluştu. Kontrollerde değerler execution'ın **POM configuration**
  bölümünde değiştirildi. Root Maven reactor build'i de sample generation execution'ını çalıştırmaz.

`addTestCompileSourceRoot=true` bu kullanımın kestirmesi değildir: official Maven plugin descriptor'ı
bunun generated **main** output'unu test classpath'ine taşıdığını ve `addCompileSourceRoot` ile karşılıklı
exclusive olduğunu açıklar. Ayrı generated test dizinini main kaynakları bozmadan eklemek farklı iştir.

## 3. Hangi flag'ler etkileniyor?

Her satır fresh output ile, önce normal sample, sonra test-root + JUnit profiliyle çalıştırıldı:

| Açık flag | Generated test / docs dosyası | Generation + main compile | Gerçek test compile | Ek bulgu |
| --- | --- | --- | --- | --- |
| Hiçbiri | 0 / 0 | Geçti | Geçti, test yok | Baseline |
| `generateApiDocumentation` | 0 / 3 | Geçti | Geçti, test yok | Markdown Java örnekleri ayrıca derlenince başarısız |
| `generateApiTests` | 3 / 0 | Geçti | Geçti | RestClient API test class'ları `@Disabled` |
| `generateModelDocumentation` | 0 / 5 | Geçti | Geçti, test yok | Wrapper → `Meta.md` linkleri kırık |
| `generateModelTests` | 5 / 0 | Geçti | Geçti | Boş TODO testleri; suppressed modellerin testleri yok |
| Dördü birlikte | 8 / 8 | Geçti | Geçti | İki documentation kusuru birlikte görülüyor |

“All” için Surefire: **22 test, 0 failure/error, 3 skipped**. Skipped olanlar upstream `@Disabled`
API testleri; diğer test gövdeleri TODO/boş. Bu başarı generic contract davranışı için yeni coverage kanıtı değildir.
API docs açık, model docs kapalıysa model linklerinin yokluğu ayrıca flag kombinasyonunun doğal sonucudur;
yukarıdaki suppressed-model link kusuru **iki documentation flag'i birlikte açıkken de** doğrulandı.

## 4. Generated source'taki somut failure

Generated `*Test.java` sınıflarında, JUnit ve root kaydı sağlandığında compilation failure yok.
Wrapper model test'i yalnız `new ServiceResponseUploadResultDto()` ve TODO metotları üretiyor;
removed `Meta` için test veya import üretmiyor. Mevcut inherited no-arg constructor yeterli.

Buna karşılık generated `docs/MultipartTransportControllerApi.md` Java bloğu şunları içeriyor:

```java
import /* invokerPackage */.ApiException;
import /* invokerPackage */.Configuration;
import /* invokerPackage */.models.*;
ApiClient defaultClient = Configuration.getDefaultApiClient();
```

RestClient output'unda `ApiException` ve `Configuration` yok; modeller configured `modelPackage` altında,
`invokerPackage.models` altında değil. Verbatim Java blokları geçici test source'a kopyalandı; yalnız
aynı isimli `public class Example` çakışmalarını önlemek için class isimleri değiştirildi. Hem platform
hem stock `java` kontrolü `cannot find symbol: ApiException / Configuration` ve
`package ...invoker.models does not exist` hatalarını verdi.

Binary documentation ayrıca `File result = apiInstance.download(documentId)` yazıyor; gerçek metot
`org.springframework.core.io.Resource` döndürüyor. Import/bootstrap hatalarını ayıran tek-metot probe'u:

```java
void example(BinaryTransportControllerApi api) {
    java.io.File result = api.download("document-1");
}
// incompatible types: org.springframework.core.io.Resource cannot be converted to java.io.File
```

Markdown'ın kendi generation'ı başarılıdır; bu Java blokları normal build'e dahil olmadığından
`generateApiDocumentation=true` **tek başına** Maven compilation failure yaratmaz.

## 5. Root cause ve ek kontroller

**Test discovery:** output generation, Maven test-root registration, test dependencies ve test execution
ayrı aşamalar. Mevcut parent main sources'u kaydeder/normalize eder; generated test root'unu kaydetmez.

**Model docs:** `GenericAwareJavaCodegen.postProcessModels/postProcessAllModels` ignored/BYOC modelleri
render graph'ından çıkarır ve ignored import'ları temizler. `DefaultGenerator` aynı filtered graph'tan
model, model test ve model doc üretir. Dolayısıyla suppressed modelin üç artifact'i de yoktur.
Ancak retained wrapper'ın `CodegenModel.vars` bilgisi OpenAPI'nin flattened `data/meta` şemasını korur.
`api_wrapper.mustache` Java gövdesini shared generic inheritance'a çevirirken ordinary documentation
partial'ı hâlâ `vars` referanslarından yerel `.md` linkleri kurar. Wire shape'i açıklamak kendi başına
hata değildir; üretilmeyen dosyalara link vermek ve Java binding kimliğini belirtmemek kusurdur.

- Transport “all” çıktısında iki wrapper `Meta.md` dosyasına link veriyor; dosya yok.
- Page+BYOC kontrolünde canonical Gradle sample'ın committed spec'i, aynı Maven probe'a input oldu.
  Handwritten transport adapter kaynakları çıkarıldı; canonical pure-Java `CustomerDto` source'u
  classpath sağlayacak biçimde kopyalandı ve
  `openapi-generics.response-contract.CustomerDto=io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto`
  eklendi. Bütün flag'ler açık: **4 test source + 4 doc**, generation/test compile geçti;
  Surefire **15 test, 0 failure/error, 2 skipped**.
- Bu kontrolde `ServiceResponsePageCustomerDto.md` → `PageCustomerDto.md` ve `Meta.md`, direct wrapper
  doc'u → `CustomerDto.md` linkleri kırık. Java binding ise doğru: `ServiceResponse<Page<CustomerDto>>`;
  BYOC/suppressed sınıfların duplicate model/test/doc dosyaları yok.
- Stock `java` kontrolünde aynı transport spec/library/options kullanıldı; platform template lifecycle
  skip edildi, empty consumer template directory ile embedded upstream template'lere dönüldü ve yalnız
  platform wrapper inheritance'ı bekleyen handwritten adapter source'ları çıkarıldı. **10 test source +
  10 doc**, generation/test compile geçti. API snippet hataları ve `File.md` kırık linki burada da var;
  `Meta.md` ise stock model graph'ında bulunduğundan wrapper-link sorunu yok.

## 6. Upstream template / origin

Kaynaklar kurulu **`org.openapitools:openapi-generator:7.27.0` JAR ve sources JAR** içinden doğrudan okundu;
başka sürüm dokümanından veya hatırlanan template davranışından çıkarım yapılmadı.

| Origin | İlgili davranış |
| --- | --- |
| `Java/libraries/restclient/api_test.mustache:24,38–40` | `@Disabled`; File/Resource için library-aware parametre/dönüş koşulları var. API testleri bu nedenle binary/multipart için de derleniyor. |
| `Java/model_test.mustache:18–39` | Model instantiation + `allVars` üzerinden TODO testleri; gerçek getter/setter assert'i yok. |
| `Java/model_doc.mustache` → `Java/pojo_doc.mustache:10` | Ordinary schema vars'ından model linkleri; wrapper/shared-contract bilgisine özel branch yok. |
| `Java/api_doc.mustache:27–36,60–65` | Yanlış invoker bootstrap/importlar; raw datatype/returnType, Resource uyarlaması yok. RestClient-specific `api_doc.mustache` bulunmadığından generic template kullanılıyor. |
| `DefaultGenerator.java:532,550–600` | Global model post-processing'den sonra aynı retained graph üzerinden model/test/doc üretimi. |
| `DefaultGenerator.java:391–407` | Var olan model test dosyaları overwrite edilmiyor; karşılaştırmalar `clean` ile yapılmalı. |
| `AbstractJavaCodegen.java:938–943` | Test dosyası yolu `outputTestFolder/src/test/java/<package>`; `sourceFolder=src/gen/java` main root'undan ayrı. |
| `GeneratorTemplateContentLocator.java:54–60,84–116` | Consumer library template → consumer root → embedded library → embedded root önceliği. |

Platform source origin'leri: `GenericAwareJavaCodegen`, `ExternalImportResolver`, `EnvelopeMetadataResolver`
ve `META-INF/openapi-generics/templates/api_wrapper.mustache`.
Maven resource overlay ve Gradle `TemplateOverlayCopier` nested kaynakları zaten kopyalıyor;
yeni resource overlay fikri için ayrı generation lifecycle icat etmek gerekmiyor.

## 7–9. Olası çözümler, değişiklik yüzeyi / bakım / mimari risk

Bunlar **değerlendirme**, uygulanmış veya prototype ile doğrulanmış fix'ler değildir.

| Seçenek | Değişiklik yüzeyi / bakım maliyeti | Risk / değerlendirme |
| --- | --- | --- |
| Testler için codegen fix'i yapmamak; generated test root/JUnit ve scaffold sınırını açıklamak | Consumer/sample build veya doküman; core değişmez. Düşük bakım. | Doğrulanan durumda yeterli. Her false flag'i “unsupported” ilan etmek yanlış; generation/compile çalışıyor. Otomatik test wiring ayrı, açık bir build-integration feature kararıdır. |
| **RestClient-scope wrapper doc dispatch + küçük yeni partial** | Codegen artifact'inde `libraries/restclient/model_doc.mustache` ve wrapper doc partial'ı; wrapper/Page/BYOC + ordinary model/enum doc regression'ları. Mevcut `x-api-wrapper`, envelope/container/type/item ve `x-extra-imports` metadata'sı yeterli olabilir. Düşük–orta bakım. | En temiz küçük follow-up adayı. Wrapper için gerçek Java binding'i gösterip shared tipleri olmayan yerel `.md` linklerine çevirmemeli; ordinary RestClient enum/POJO dispatch'i upstream partial'lara bırakmalı. Yeni producer extension, Page-specific branch veya model graph değişimi gerekmez. Diğer library'ler için doğrulama ayrıca gerekir. |
| Tüm modeller için doc-only external-reference metadata'sı / generic `pojo_doc` uyarlaması | `postProcessModels` içinde property-level işaretleme + shared partial değişikliği + geniş model/link testleri. Orta bakım. | Wrapper-only ihtiyaca göre daha geniş. `vars`, imports veya datatype'ı docs düzeltmek için semantik olarak değiştirmek ordinary generation'ı bozabilir; yalnız ek doc metadata'sı bile kapsamı büyütür. İlk seçenek olmamalı. |
| RestClient API docs için local overlay veya content rewrite | Library-specific yaklaşık 108 satırlık API doc template'inin sahipliği ya da import/bootstrap/File/Resource/auth/example dönüşümlerini sürdüren post-processing. Orta–yüksek bakım. | Teknik olarak lokal olabilir ama sırf wrapper specialization adına upstream transport/docs sahipliğini platforma taşır. Birkaç import silmek yeterli değil. Upstream fix/issue daha uygun; geniş local fork önermiyorum. |
| Ignored modelleri sırf doc üretmek için render graph'a geri eklemek veya ayrı doc generation pipeline'ı | Filtering/lifecycle değişikliği, output bazında farklı graph, test/model suppression denetimleri. Yüksek bakım. | Duplicate shared modelleri yeniden üretebilir, authority/suppression sınırını bozar. Bu sorun için orantısız; önerilmez. |

Global root `model_doc.mustache` overlay'i otomatik olarak güvenli değildir: consumer root template'i
embedded **library** template'inden önce gelir. Örneğin Jersey 2/3 kendi oneOf/anyOf doc dispatch'ini
kullanıyor. Root'a basit POJO fallback koymak ordinary upstream davranışı gölgeleyebilir. Küçük follow-up
ilk etapta test edilen library'ye scope edilmeli; tüm library'lere evrensel fix diye sunulmamalı.
Post-processing de serbest bakım değildir: generated dosya metninden shared type tahmin etmek kırılgan;
model metadata'sıyla template seviyesinde karar vermek daha temizdir.

## 10. Öneri ve kanıt sınırı

1. **Generated testler için production codegen değişikliği yapma.** İncelenen wrapper/suppression/BYOC
   yollarında compile problemi yok; testlerin oluşmadığı iddiası da gerçek dosyalarla kapandı.
   Test root'u ve JUnit wiring'i isteyen consumer açıkça sağlamalı; scaffolding platform acceptance testi değildir.
2. **Wrapper model documentation için küçük bir follow-up makul.** RestClient-scope, mevcut metadata'yı
   kullanan dispatch/partial ve kırık link + ordinary-model regression'larıyla sınırlanabilir.
   Temiz ve ucuz bir aday var; core mimariyi değiştirmek gerekmiyor. Bu investigation fix'in kendisini uygulamadı.
3. **RestClient API snippet sorununu upstream'e ait compatibility kusuru olarak tut.** Generated API
   dokümanını copy/paste-ready Java rehberi olarak garanti etme; örnekler elle doğrulanmış consumer
   dokümanıyla desteklenebilir. Bütün documentation/test flag'lerini support dışı ilan etmek veya kapatmayı
   zorlamak gerekmez. Library/genel API docs fork'u bugünkü faydaya göre gereksiz derecede pahalı.
4. OAG minimum/current/negative sınırları, contract semantics, platform architecture ve lifecycle değiştirilmedi.
   Tüm library'ler, BYOE constructor varyantları, enum/oneOf/anyOf test-output kombinasyonları ve future
   upstream sürümler exhaustive test edilmedi; bu rapor bunlar için garanti vermez.

Yerel kanıtlar `/private/tmp/oag-output-investigation/` altında: `results.json`, flag başına
`*-normal.log` / `*-compiled.log`, `all-test-execution.log`, `page-byoc.log`, `stock-java.log`,
`*-doc-snippets.log`, `binary-doc-signature.log`, `no-junit.log`, `cli-override.log`,
`missing-doc-links.json`, `evidence/` generated output'ları ve `upstream/` extracted kaynakları.
Bu yollar geçici; somut snippet/diagnostic ve tekrar üretim adımları yukarıda kalıcı olarak kaydedildi.
Production/sample diff, feature implementation, commit veya push yapılmadı.


## Follow-up implementation — 2026-11-14

RestClient-specific `libraries/restclient/model_doc.mustache` dispatch'i ve `api_wrapper_doc.mustache`
partial'ı eklendi. Partial mevcut `api_wrapper.mustache` Java binding fragment'ini kullanıyor; wrapper
belgesinde shared envelope/container/BYOC kimlikleri düz metin olarak gösteriliyor, suppressed yerel
model dokümanlarına link üretilmiyor. Global model-doc override, producer metadata, model graph veya
lifecycle değişikliği yok. Generated tests ve upstream API documentation snippet'leri değiştirilmedi.

`WrapperModelDocumentationTest`: direct/Page + BYOC on/off + BYOE, ordinary model/enum çıktısının
upstream ile byte equality kontrolü ve WebClient isolation (4 başarılı test invocation'ı).
Root Maven clean install, Maven samples verify ve Gradle check/e2eTest geçti. Paketli artifact ayrıca
ayrı Maven consumer'da 7.27.0 ve 7.25.0 ile bütün output flag'leri açıkken generation/compile/test'ten
geçti; API doc örneklerinin upstream davranışı bu fix'in kapsamı dışında kaldı.
Log'lar: `/private/tmp/wrapper-doc-tests.log`, `/private/tmp/public-docs-root.log`,
`/private/tmp/public-docs-generation.log`, `/private/tmp/public-docs-generation725.log`,
`/private/tmp/public-docs-samples.log`, `/private/tmp/public-docs-gradle-plugin.log`.
Eski investigation sonuçları yeni API-doc/test support garantisi olarak okunmamalı.
