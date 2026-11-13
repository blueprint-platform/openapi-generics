package io.github.blueprintplatform.samples.gradle.customer.producer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiSnapshotTest {
  @Autowired MockMvc mvc;

  @Test
  void producerContractMatchesCommittedSnapshot() throws Exception {
    var mapper = JsonMapper.builder().build();
    var actual =
        mapper.readTree(
            mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
    var generated = Path.of(System.getProperty("sample.generatedSnapshot"));
    Files.createDirectories(generated.getParent());
    Files.writeString(
        generated, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual) + "\n");
    var committed = Path.of(System.getProperty("sample.snapshot"));
    if (Boolean.getBoolean("sample.updateSnapshot")) {
      Files.copy(generated, committed, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    assertThat(actual).isEqualTo(mapper.readTree(Files.readString(committed)));
    var schemas = actual.path("components").path("schemas");
    assertThat(schemas.path("ServiceResponseCustomerDto").path("x-api-wrapper").asBoolean())
        .isTrue();
    assertThat(schemas.path("ServiceResponsePageCustomerDto").path("x-data-container").asText())
        .isEqualTo("Page");
    assertThat(schemas.path("PageCustomerDto").path("x-ignore-model").asBoolean()).isTrue();
  }
}
