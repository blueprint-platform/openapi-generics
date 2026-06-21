package io.github.blueprintplatform.samples.customerservice.common.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiSnapshotGenerationTest {

    private static final String API_DOCS_YAML = "/v3/api-docs.yaml";

    private static final Path OUTPUT = Path.of("..", "spec", "customer-api-docs.yaml");

    private static final String OPENAPI_MARKER = "openapi:";

    private static final String API_WRAPPER_EXTENSION = "x-api-wrapper";
    private static final String API_WRAPPER_DATATYPE_EXTENSION = "x-api-wrapper-datatype";
    private static final String DATA_CONTAINER_EXTENSION = "x-data-container";
    private static final String DATA_ITEM_EXTENSION = "x-data-item";
    private static final String IGNORE_MODEL_EXTENSION = "x-ignore-model";

    private static final String CUSTOMER_WRAPPER_SCHEMA = "ServiceResponseCustomerDto";
    private static final String PAGE_CUSTOMER_WRAPPER_SCHEMA = "ServiceResponsePageCustomerDto";
    private static final String PAGE_CUSTOMER_SCHEMA = "PageCustomerDto";
    private static final String CUSTOMER_SCHEMA = "CustomerDto";

    private static final String PAGE_CONTAINER_MARKER = "x-data-container: Page";
    private static final String CUSTOMER_ITEM_MARKER = "x-data-item: CustomerDto";

    @Autowired private MockMvc mockMvc;

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
                        CUSTOMER_WRAPPER_SCHEMA,
                        PAGE_CUSTOMER_WRAPPER_SCHEMA,
                        PAGE_CUSTOMER_SCHEMA,
                        CUSTOMER_SCHEMA,
                        PAGE_CONTAINER_MARKER,
                        CUSTOMER_ITEM_MARKER);

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