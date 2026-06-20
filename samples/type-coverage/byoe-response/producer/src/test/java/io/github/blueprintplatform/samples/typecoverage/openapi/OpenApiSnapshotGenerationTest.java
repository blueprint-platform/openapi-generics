package io.github.blueprintplatform.samples.typecoverage.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiSnapshotGenerationTest {

    private static final String API_DOCS_YAML = "/v3/api-docs.yaml";

    private static final Path OUTPUT = Path.of("..", "spec", "byoe-response-coverage.yaml");

    private static final String OPENAPI_MARKER = "openapi:";

    private static final String API_WRAPPER_EXTENSION = "x-api-wrapper";
    private static final String API_WRAPPER_DATATYPE_EXTENSION = "x-api-wrapper-datatype";
    private static final String IGNORE_MODEL_EXTENSION = "x-ignore-model";

    private static final String SIMPLE_WRAPPER_SCHEMA = "ApiResponseString";
    private static final String DTO_WRAPPER_SCHEMA = "ApiResponseTypeProfileDto";
    private static final String ENUM_WRAPPER_SCHEMA = "ApiResponseCoverageStatus";
    private static final String UUID_WRAPPER_SCHEMA = "ApiResponseUUID";
    private static final String DATE_WRAPPER_SCHEMA = "ApiResponseLocalDate";
    private static final String DATETIME_WRAPPER_SCHEMA = "ApiResponseOffsetDateTime";
    private static final String DECIMAL_WRAPPER_SCHEMA = "ApiResponseBigDecimal";

    @Autowired private MockMvc mockMvc;

    @Test
    void generateOpenApiYamlSnapshot() throws Exception {
        String yaml = fetchOpenApiYaml();

        assertThat(yaml)
                .contains(
                        OPENAPI_MARKER,
                        API_WRAPPER_EXTENSION,
                        API_WRAPPER_DATATYPE_EXTENSION,
                        IGNORE_MODEL_EXTENSION,
                        SIMPLE_WRAPPER_SCHEMA,
                        DTO_WRAPPER_SCHEMA,
                        ENUM_WRAPPER_SCHEMA,
                        UUID_WRAPPER_SCHEMA,
                        DATE_WRAPPER_SCHEMA,
                        DATETIME_WRAPPER_SCHEMA,
                        DECIMAL_WRAPPER_SCHEMA);

        writeSnapshot(yaml);
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