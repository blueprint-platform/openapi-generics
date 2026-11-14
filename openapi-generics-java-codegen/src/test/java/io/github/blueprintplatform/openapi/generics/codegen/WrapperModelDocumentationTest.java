package io.github.blueprintplatform.openapi.generics.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

class WrapperModelDocumentationTest {
  @TempDir Path directory;

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void documentsSharedBindingsWithoutLinksToSuppressedOrReusedModels(boolean byoc)
      throws Exception {
    var output = generate("restclient", true, byoc, "result");
    var direct = Files.readString(output.resolve("docs/ServiceResponseCustomerDto.md"));
    var page = Files.readString(output.resolve("docs/ServiceResponsePageCustomerDto.md"));
    var byoe = Files.readString(output.resolve("docs/ApiResponseCustomerDto.md"));
    assertTrue(direct.contains("extends ServiceResponse<CustomerDto>"), direct);
    assertTrue(page.contains("extends ServiceResponse<Page<CustomerDto>>"), page);
    assertTrue(byoe.contains("extends ApiResponse<CustomerDto>"), byoe);
    assertTrue(
        page.contains("io.github.blueprintplatform.openapi.generics.contract.paging.Page"), page);
    assertTrue(byoe.contains("com.example.contract.ApiResponse"), byoe);
    for (var doc : new String[] {direct, page, byoe}) {
      assertFalse(doc.contains(".md)"), doc);
      assertTrue(doc.contains("transport binding"), doc);
    }
    assertFalse(Files.exists(output.resolve("docs/Meta.md")));
    assertFalse(Files.exists(output.resolve("docs/PageCustomerDto.md")));
    assertEquals(!byoc, Files.exists(output.resolve("docs/CustomerDto.md")));
    if (byoc) {
      assertTrue(direct.contains("Reused payload: `com.example.contract.CustomerDto`"), direct);
      assertTrue(page.contains("import com.example.contract.CustomerDto;"), page);
    }
  }

  @Test
  void ordinaryModelAndEnumDocumentationRemainIdenticalToUpstream() throws Exception {
    var baseline = generate("restclient", false, false, "baseline");
    var overlaid = generate("restclient", true, false, "overlaid");
    for (var name : new String[] {"OrderDto", "Status", "CustomerDto"}) {
      assertEquals(
          Files.readString(baseline.resolve("docs/" + name + ".md")),
          Files.readString(overlaid.resolve("docs/" + name + ".md")),
          name);
    }
  }

  @Test
  void overlayDoesNotChangeOtherLibraryDocumentation() throws Exception {
    var baseline = generate("webclient", false, true, "baseline");
    var overlaid = generate("webclient", true, true, "overlaid");
    for (var name :
        new String[] {
          "OrderDto",
          "Status",
          "ServiceResponseCustomerDto",
          "ServiceResponsePageCustomerDto",
          "ApiResponseCustomerDto"
        }) {
      assertEquals(
          Files.readString(baseline.resolve("docs/" + name + ".md")),
          Files.readString(overlaid.resolve("docs/" + name + ".md")),
          name);
    }
  }

  private Path generate(String library, boolean overlay, boolean byoc, String name)
      throws Exception {
    var output = directory.resolve(name);
    var config =
        new CodegenConfigurator()
            .setGeneratorName("java-generics-contract")
            .setLibrary(library)
            .setInputSpec(
                Path.of(getClass().getResource("/wrapper-model-docs.yaml").toURI()).toString())
            .setOutputDir(output.toString())
            .setModelPackage("com.example.generated.model")
            .addGlobalProperty("models", "")
            .addGlobalProperty("modelDocs", "true")
            .addGlobalProperty("modelTests", "false")
            .addAdditionalProperty("openApiNullable", false)
            .addAdditionalProperty("hideGenerationTimestamp", true);
    if (byoc) {
      config.addAdditionalProperty(
          "openapi-generics.response-contract.CustomerDto", "com.example.contract.CustomerDto");
    }
    if (overlay) {
      var templates = directory.resolve("templates");
      copyOverlay("api_wrapper.mustache", templates);
      copyOverlay("api_wrapper_doc.mustache", templates);
      copyOverlay("libraries/restclient/model_doc.mustache", templates);
      config.setTemplateDir(templates.toString());
    }
    new DefaultGenerator().opts(config.toClientOptInput()).generate();
    return output;
  }

  private void copyOverlay(String name, Path destination) throws IOException {
    var path = destination.resolve(name);
    Files.createDirectories(path.getParent());
    try (var source =
        getClass().getResourceAsStream("/META-INF/openapi-generics/templates/" + name)) {
      Files.copy(source, path);
    }
  }
}
