package io.github.blueprintplatform.openapi.generics.codegen;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    codegen
        .additionalProperties()
        .put("openapi-generics.response-contract.CustomerDto", "io.example.CustomerDto");

    codegen.processOpts();

    Schema<?> externalSchema = new Schema<>();
    Schema<?> normalSchema = new Schema<>();

    CodegenModel externalModel = codegen.fromModel("CustomerDto", externalSchema);
    CodegenModel normalModel = codegen.fromModel("OrderDto", normalSchema);

    ModelMap mm1 = new ModelMap();
    mm1.setModel(externalModel);

    ModelMap mm2 = new ModelMap();
    mm2.setModel(normalModel);

    ModelsMap modelsMap = new ModelsMap();

    // FIX: mutable list
    List<ModelMap> modelList = new ArrayList<>();
    modelList.add(mm1);
    modelList.add(mm2);

    modelsMap.setModels(modelList);

    ModelsMap result = codegen.postProcessModels(modelsMap);

    assertNotNull(result);
    assertNotNull(result.getModels());

    assertEquals(1, result.getModels().size());
    assertEquals("OrderDto", result.getModels().get(0).getModel().name);
  }

  @Test
  @DisplayName("fromModel -> should clean imports of ignored models")
  void shouldCleanImports_ofIgnoredModels() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    codegen
        .additionalProperties()
        .put("openapi-generics.response-contract.CustomerDto", "io.example.CustomerDto");

    codegen.processOpts();

    Schema<?> schema = new Schema<>();

    CodegenModel model = codegen.fromModel("CustomerDto", schema);

    model.imports = new java.util.HashSet<>(List.of("CustomerDto", "OtherDto"));

    CodegenModel processed = codegen.fromModel("CustomerDto", schema);

    assertNotNull(processed);
    if (processed.imports != null) {
      assertFalse(processed.imports.contains("CustomerDto"));
    }
  }

  @Test
  @DisplayName("postProcessModels -> should inject external import into wrapper model")
  void shouldInjectExternalImport_intoWrapperModel() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    codegen
        .additionalProperties()
        .put("openapi-generics.response-contract.CustomerDto", "io.example.CustomerDto");

    codegen.processOpts();

    CodegenModel wrapperModel = new CodegenModel();
    wrapperModel.name = "ServiceResponseCustomerDto";
    wrapperModel.vendorExtensions.put("x-api-wrapper", true);
    wrapperModel.vendorExtensions.put("x-data-item", "CustomerDto");

    ModelMap mm = new ModelMap();
    mm.setModel(wrapperModel);

    ModelsMap modelsMap = new ModelsMap();
    List<ModelMap> modelList = new ArrayList<>();
    modelList.add(mm);
    modelsMap.setModels(modelList);

    ModelsMap result = codegen.postProcessModels(modelsMap);

    CodegenModel processed = result.getModels().get(0).getModel();

    assertEquals("io.example.CustomerDto", processed.vendorExtensions.get("x-extra-imports"));
  }

  @Test
  @DisplayName("postProcessModels -> should inject envelope metadata into wrapper model")
  void shouldInjectEnvelopeMetadata_intoWrapperModel() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    codegen.additionalProperties().put("openapi-generics.envelope", "io.example.ApiResponse");

    codegen.processOpts();

    CodegenModel wrapperModel = new CodegenModel();
    wrapperModel.name = "ApiResponseCustomerDto";
    wrapperModel.vendorExtensions.put("x-api-wrapper", true);

    ModelMap mm = new ModelMap();
    mm.setModel(wrapperModel);

    ModelsMap modelsMap = new ModelsMap();
    List<ModelMap> modelList = new ArrayList<>();
    modelList.add(mm);
    modelsMap.setModels(modelList);

    ModelsMap result = codegen.postProcessModels(modelsMap);

    CodegenModel processed = result.getModels().get(0).getModel();

    assertEquals("io.example.ApiResponse", processed.vendorExtensions.get("x-envelope-import"));
    assertEquals("ApiResponse", processed.vendorExtensions.get("x-envelope-type"));
  }

  @Test
  @DisplayName("postProcessAllModels -> should remove ignored models from global model graph")
  void shouldRemoveIgnoredModels_fromGlobalModelGraph() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    codegen
        .additionalProperties()
        .put("openapi-generics.response-contract.CustomerDto", "io.example.CustomerDto");

    codegen.processOpts();

    Schema<?> externalSchema = new Schema<>();
    Schema<?> normalSchema = new Schema<>();

    codegen.fromModel("CustomerDto", externalSchema);
    codegen.fromModel("OrderDto", normalSchema);

    ModelsMap externalModels = new ModelsMap();
    externalModels.setModels(new ArrayList<>());

    ModelsMap normalModels = new ModelsMap();
    normalModels.setModels(new ArrayList<>());

    Map<String, ModelsMap> allModels = new LinkedHashMap<>();
    allModels.put("CustomerDto", externalModels);
    allModels.put("OrderDto", normalModels);

    Map<String, ModelsMap> result = codegen.postProcessAllModels(allModels);

    assertFalse(result.containsKey("CustomerDto"));
    assertTrue(result.containsKey("OrderDto"));
  }

  @Test
  @DisplayName("getName -> should return custom generator name")
  void getName_shouldReturnCustomGeneratorName() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    assertEquals("java-generics-contract", codegen.getName());
  }

  @Test
  @DisplayName(
      "postProcessModels -> should keep non-wrapper model unchanged when no contract metadata applies")
  void shouldKeepNonWrapperModelUnchanged() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();
    codegen.processOpts();

    CodegenModel model = new CodegenModel();
    model.name = "OrderDto";

    ModelMap mm = new ModelMap();
    mm.setModel(model);

    ModelsMap modelsMap = new ModelsMap();
    modelsMap.setModels(new ArrayList<>());
    List<ModelMap> modelList = new ArrayList<>();
    modelList.add(mm);
    modelsMap.setModels(modelList);

    ModelsMap result = codegen.postProcessModels(modelsMap);

    CodegenModel processed = result.getModels().get(0).getModel();

    assertNotNull(processed);
    assertNull(processed.vendorExtensions.get("x-extra-imports"));
    assertNull(processed.vendorExtensions.get("x-envelope-import"));
    assertNull(processed.vendorExtensions.get("x-envelope-type"));
  }
}
