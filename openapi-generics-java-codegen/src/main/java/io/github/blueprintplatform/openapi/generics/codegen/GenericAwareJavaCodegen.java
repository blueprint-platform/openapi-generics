package io.github.blueprintplatform.openapi.generics.codegen;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.model.ModelsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generics-aware extension of JavaClientCodegen.
 *
 * <p>Aligns generated client models with the canonical API contract by:
 *
 * <ul>
 *   <li>Suppressing generation of shared/external models (BYOC)
 *   <li>Injecting required imports for externally provided types
 *   <li>Applying envelope metadata for wrapper models (BYOE)
 * </ul>
 *
 * <p>Ensures generated clients remain contract-aligned, with no duplication or drift.
 */
public class GenericAwareJavaCodegen extends JavaClientCodegen {

  private static final Logger log = LoggerFactory.getLogger(GenericAwareJavaCodegen.class);

  private final ExternalModelRegistry registry = new ExternalModelRegistry();
  private final ModelIgnoreDecider ignoreDecider = new ModelIgnoreDecider(registry);
  private final ExternalImportResolver importResolver = new ExternalImportResolver(registry);
  private final EnvelopeMetadataResolver envelopeResolver = new EnvelopeMetadataResolver();

  @Override
  public void processOpts() {
    super.processOpts();
    registry.register(additionalProperties);
    envelopeResolver.register(additionalProperties);

    log.debug(
        "Generic-aware codegen initialized with external model registry and envelope metadata");
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
              }
            });

    return result;
  }

  @Override
  public Map<String, ModelsMap> postProcessAllModels(Map<String, ModelsMap> allModels) {
    Map<String, ModelsMap> result = super.postProcessAllModels(allModels);

    int before = result.size();

    result.entrySet().removeIf(e -> ignoreDecider.isIgnored(e.getKey()));

    int after = result.size();

    if (before != after) {
      log.debug("Removed ignored models from global model graph: {} -> {}", before, after);
    }

    return result;
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