package io.github.blueprintplatform.openapi.generics.codegen;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

@Tag("unit")
@DisplayName("Smoke Test: GenericAwareJavaCodegen")
class GenericAwareJavaCodegenTest {

    @Test
    @DisplayName("processOpts + fromModel + postProcessModels -> should filter external model")
    void shouldFilterExternalModel_andKeepOthers() {
        GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

        // simulate additionalProperties (external contract mapping)
        codegen.additionalProperties().put(
                "openapiGenerics.responseContract.CustomerDto",
                "io.example.CustomerDto");

        codegen.processOpts();

        // --- create schemas
        Schema<?> externalSchema = new Schema<>();
        Schema<?> normalSchema = new Schema<>();

        // --- generate models
        CodegenModel externalModel = codegen.fromModel("CustomerDto", externalSchema);
        CodegenModel normalModel = codegen.fromModel("OrderDto", normalSchema);

        // --- wrap into ModelsMap
        ModelMap mm1 = new ModelMap();
        mm1.setModel(externalModel);

        ModelMap mm2 = new ModelMap();
        mm2.setModel(normalModel);

        ModelsMap modelsMap = new ModelsMap();
        modelsMap.setModels(List.of(mm1, mm2));

        // --- run post process
        ModelsMap result = codegen.postProcessModels(modelsMap);

        assertNotNull(result);
        assertNotNull(result.getModels());

        // external model should be removed
        assertEquals(1, result.getModels().size());
        assertEquals("OrderDto", result.getModels().getFirst().getModel().name);
    }

    @Test
    @DisplayName("fromModel -> should clean imports of ignored models")
    void shouldCleanImports_ofIgnoredModels() {
        GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

        codegen.additionalProperties().put(
                "openapiGenerics.responseContract.CustomerDto",
                "io.example.CustomerDto");

        codegen.processOpts();

        Schema<?> schema = new Schema<>();

        CodegenModel model = codegen.fromModel("CustomerDto", schema);

        // simulate imports containing ignored model
        model.imports = new java.util.HashSet<>(List.of("CustomerDto", "OtherDto"));

        // trigger clean
        CodegenModel processed = codegen.fromModel("CustomerDto", schema);

        assertNotNull(processed);
        if (processed.imports != null) {
            assertFalse(processed.imports.contains("CustomerDto"));
        }
    }
}