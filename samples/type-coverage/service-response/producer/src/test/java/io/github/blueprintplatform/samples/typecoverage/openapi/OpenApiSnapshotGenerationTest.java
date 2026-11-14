package io.github.blueprintplatform.samples.typecoverage.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(OpenApiSnapshotGenerationTest.SynchronousProjection.class)
class OpenApiSnapshotGenerationTest {

  private static final String API_DOCS_YAML = "/v3/api-docs.yaml";

  private static final Path OUTPUT = Path.of("..", "spec", "service-response-coverage.yaml");

  private static final String OPENAPI_MARKER = "openapi:";

  private static final String API_WRAPPER_EXTENSION = "x-api-wrapper";
  private static final String API_WRAPPER_DATATYPE_EXTENSION = "x-api-wrapper-datatype";
  private static final String DATA_CONTAINER_EXTENSION = "x-data-container";
  private static final String DATA_ITEM_EXTENSION = "x-data-item";
  private static final String IGNORE_MODEL_EXTENSION = "x-ignore-model";

  private static final String SIMPLE_WRAPPER_SCHEMA = "ServiceResponseString";
  private static final String DTO_WRAPPER_SCHEMA = "ServiceResponseTypeProfileDto";
  private static final String ENUM_WRAPPER_SCHEMA = "ServiceResponseCoverageStatus";
  private static final String LIST_DTO_WRAPPER_SCHEMA = "ServiceResponseListTypeSummaryDto";
  private static final String LIST_ENUM_WRAPPER_SCHEMA = "ServiceResponseListCoverageStatus";
  private static final String PAGE_DTO_WRAPPER_SCHEMA = "ServiceResponsePageTypeSummaryDto";
  private static final String PAGE_ENUM_WRAPPER_SCHEMA = "ServiceResponsePageCoverageStatus";

  private static final String LIST_CONTAINER_MARKER = "x-data-container: List";
  private static final String PAGE_CONTAINER_MARKER = "x-data-container: Page";
  private static final String TYPE_SUMMARY_ITEM_MARKER = "x-data-item: TypeSummaryDto";
  private static final String COVERAGE_STATUS_ITEM_MARKER = "x-data-item: CoverageStatus";

  private static final String ASYNC_DIRECT_PATH = "/types/async/address:";
  private static final String ASYNC_PAGE_PATH = "/types/async/paged-summaries:";
  private static final String ASYNC_DIRECT_OPERATION = "operationId: completionStageAddress";
  private static final String ASYNC_PAGE_OPERATION = "operationId: deferredPagedSummaries";
  private static final String ASYNC_CONTROLLER_TAG = "async-payload-controller";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void generateOpenApiYamlSnapshot() throws Exception {
    String yaml = fetchOpenApiYaml();

    assertThat(yaml)
        .contains(
            OPENAPI_MARKER,
            API_WRAPPER_EXTENSION,
            API_WRAPPER_DATATYPE_EXTENSION,
            DATA_CONTAINER_EXTENSION,
            DATA_ITEM_EXTENSION,
            IGNORE_MODEL_EXTENSION,
            SIMPLE_WRAPPER_SCHEMA,
            DTO_WRAPPER_SCHEMA,
            ENUM_WRAPPER_SCHEMA,
            LIST_DTO_WRAPPER_SCHEMA,
            LIST_ENUM_WRAPPER_SCHEMA,
            PAGE_DTO_WRAPPER_SCHEMA,
            PAGE_ENUM_WRAPPER_SCHEMA,
            LIST_CONTAINER_MARKER,
            PAGE_CONTAINER_MARKER,
            TYPE_SUMMARY_ITEM_MARKER,
            COVERAGE_STATUS_ITEM_MARKER,
            ASYNC_DIRECT_PATH,
            ASYNC_PAGE_PATH,
            ASYNC_DIRECT_OPERATION,
            ASYNC_PAGE_OPERATION,
            ASYNC_CONTROLLER_TAG);

    writeSnapshot(yaml);
  }

  @ParameterizedTest
  @CsvSource({
    "/types/async/address, /types/objects/address, ServiceResponseAddressDto, AddressDto",
    "/types/async/paged-summaries, /types/pages/summaries, ServiceResponsePageTypeSummaryDto,"
        + " PageTypeSummaryDto"
  })
  void asyncResponseProjectsTheSameGenericContractAsItsSynchronousEquivalent(
      String asyncPath, String synchronousPath, String wrapper, String payload) throws Exception {
    JsonNode document = fetchOpenApiJson("/v3/api-docs");
    JsonNode asyncSchema = responseSchema(document, asyncPath);
    JsonNode synchronousSchema = responseSchema(document, synchronousPath);

    assertThat(asyncSchema.path("$ref").asText()).isEqualTo("#/components/schemas/" + wrapper);
    assertThat(asyncSchema).isEqualTo(synchronousSchema);

    JsonNode binding = document.path("components").path("schemas").path(wrapper);
    assertThat(binding.path("x-api-wrapper").asBoolean()).isTrue();
    assertThat(binding.path("x-api-wrapper-type").asText())
        .isEqualTo(
            "io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse");
    assertThat(binding.path("x-api-wrapper-datatype").asText()).isEqualTo(payload);
    assertThat(binding.at("/properties/data/$ref").asText())
        .isEqualTo("#/components/schemas/" + payload);
    if (payload.equals("PageTypeSummaryDto")) {
      assertThat(binding.path("x-data-container").asText()).isEqualTo("Page");
      assertThat(binding.path("x-data-container-type").asText())
          .isEqualTo("io.github.blueprintplatform.openapi.generics.contract.paging.Page");
      assertThat(binding.path("x-data-item").asText()).isEqualTo("TypeSummaryDto");
    }
  }

  @Test
  void asyncEndpointsDoNotAddOrChangeModelSchemas() throws Exception {
    JsonNode complete = fetchOpenApiJson("/v3/api-docs");
    JsonNode synchronous = fetchOpenApiJson("/v3/api-docs/synchronous");

    assertThat(complete.path("paths").has("/types/async/address")).isTrue();
    assertThat(complete.path("paths").has("/types/async/paged-summaries")).isTrue();
    assertThat(synchronous.path("paths").has("/types/async/address")).isFalse();
    assertThat(synchronous.path("paths").has("/types/async/paged-summaries")).isFalse();
    assertThat(complete.path("components").path("schemas").isObject()).isTrue();
    assertThat(complete.path("components").path("schemas"))
        .isEqualTo(synchronous.path("components").path("schemas"));
    assertThat(complete.path("components").path("schemas").has("ServiceResponseTypeSummaryDto"))
        .isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/types/async/address", "/types/async/paged-summaries"})
  void endpointsUseSpringMvcAsyncDispatch(String path) throws Exception {
    var result = mockMvc.perform(get(path)).andExpect(request().asyncStarted()).andReturn();
    mockMvc
        .perform(asyncDispatch(result))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.meta").exists());
  }

  private JsonNode fetchOpenApiJson(String path) throws Exception {
    var result = mockMvc.perform(get(path)).andExpect(status().isOk()).andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsByteArray());
  }

  private JsonNode responseSchema(JsonNode document, String path) {
    return document
        .path("paths")
        .path(path)
        .path("get")
        .path("responses")
        .path("200")
        .path("content")
        .path("application/json")
        .path("schema");
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class SynchronousProjection {
    @Bean
    GroupedOpenApi synchronousProjection(
        @Qualifier("openApiGenericsCustomizer") OpenApiCustomizer customizer) {
      return GroupedOpenApi.builder()
          .group("synchronous")
          .pathsToExclude("/types/async/**")
          .addOpenApiCustomizer(customizer)
          .build();
    }
  }

  private String fetchOpenApiYaml() throws Exception {
    return mockMvc
        .perform(get(API_DOCS_YAML))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);
  }

  private void writeSnapshot(String yaml) throws Exception {
    Files.createDirectories(OUTPUT.getParent());
    Files.writeString(OUTPUT, yaml, StandardCharsets.UTF_8);
  }
}
