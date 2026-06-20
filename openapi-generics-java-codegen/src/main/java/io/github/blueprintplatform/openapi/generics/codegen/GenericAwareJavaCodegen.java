package io.github.blueprintplatform.openapi.generics.codegen;

import io.github.blueprintplatform.openapi.generics.codegen.external.ExternalImportResolver;
import io.github.blueprintplatform.openapi.generics.codegen.external.ExternalModelRegistry;
import io.github.blueprintplatform.openapi.generics.codegen.filtering.ModelIgnoreDecider;
import io.github.blueprintplatform.openapi.generics.codegen.metadata.ContainerMetadataResolver;
import io.github.blueprintplatform.openapi.generics.codegen.metadata.EnvelopeMetadataResolver;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.model.ModelsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericAwareJavaCodegen extends JavaClientCodegen {

  private static final Logger log = LoggerFactory.getLogger(GenericAwareJavaCodegen.class);

  private final ExternalModelRegistry registry = new ExternalModelRegistry();
  private final ModelIgnoreDecider ignoreDecider = new ModelIgnoreDecider(registry);
  private final ExternalImportResolver importResolver = new ExternalImportResolver(registry);
  private final EnvelopeMetadataResolver envelopeResolver = new EnvelopeMetadataResolver();
  private final ContainerMetadataResolver containerResolver = new ContainerMetadataResolver();

  @Override
  public void processOpts() {
    super.processOpts();

    registry.register(additionalProperties);
    envelopeResolver.register(additionalProperties);
    containerResolver.register(additionalProperties);

    log.debug(
        "Generic-aware codegen initialized with external model registry, envelope metadata, and container metadata");
  }

  @Override
  public CodegenModel fromModel(String name, Schema model) {
    CodegenModel cm = super.fromModel(name, model);

    if (ignoreDecider.shouldIgnore(name, model)) {
      ignoreDecider.markIgnored(name);
    }

    cleanImports(cm);
    return cm;
  }

  @Override
  public ModelsMap postProcessModels(ModelsMap modelsMap) {
    ModelsMap result = super.postProcessModels(modelsMap);

    if (result == null || result.getModels() == null) {
      return result;
    }

    int before = result.getModels().size();

    result
        .getModels()
        .removeIf(
            m -> {
              CodegenModel model = m.getModel();
              return model != null && ignoreDecider.isIgnored(model.name);
            });

    int after = result.getModels().size();

    if (before != after) {
      log.debug("Filtered ignored models: {} -> {}", before, after);
    }

    result
        .getModels()
        .forEach(
            m -> {
              CodegenModel model = m.getModel();

              if (model != null) {
                importResolver.apply(model);
                envelopeResolver.apply(model);
                containerResolver.apply(model);
              }
            });

    return result;
  }

  @Override
  public Map<String, ModelsMap> postProcessAllModels(Map<String, ModelsMap> allModels) {
    int before = allModels.size();

    allModels.entrySet().removeIf(e -> ignoreDecider.isIgnored(e.getKey()));

    int after = allModels.size();

    if (before != after) {
      log.debug("Removed ignored models from global model graph: {} -> {}", before, after);
    }

    return super.postProcessAllModels(allModels);
  }

  @Override
  public String getName() {
    return "java-generics-contract";
  }

  private void cleanImports(CodegenModel model) {
    if (model.imports == null || model.imports.isEmpty()) {
      return;
    }

    model.imports.removeIf(ignoreDecider::isIgnored);
  }
}
