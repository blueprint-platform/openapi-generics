package io.github.blueprintplatform.openapi.generics.codegen;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds mappings between OpenAPI model names and external Java types (FQCN).
 *
 * <p>Used to prevent generation of shared contract models and reference them instead.
 *
 * <p>Configuration format:
 *
 * <pre>
 * "openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
 * </pre>
 */
public class ExternalModelRegistry {

  private static final Logger log = LoggerFactory.getLogger(ExternalModelRegistry.class);

  private static final String PREFIX = "openapi-generics.response-contract.";

  private final Map<String, String> externalModels = new HashMap<>();

  /** Registers external models from generator additionalProperties. */
  public void register(Map<String, Object> additionalProperties) {
    if (additionalProperties == null || additionalProperties.isEmpty()) {
      return;
    }

    additionalProperties.forEach(this::registerIfExternalModelProperty);
  }

  private void registerIfExternalModelProperty(String key, Object raw) {
    if (key == null || !key.startsWith(PREFIX)) {
      return;
    }

    String modelName = key.substring(PREFIX.length());
    String fqcn = normalizeFqcn(modelName, raw);

    if (fqcn != null) {
      externalModels.put(modelName, fqcn);
      log.debug("Registered external model: {} -> {}", modelName, fqcn);
    }
  }

  private String normalizeFqcn(String modelName, Object raw) {
    if (raw == null) {
      log.warn("Skipping external model '{}' because value is null", modelName);
      return null;
    }

    String fqcn = String.valueOf(raw).trim();

    if (fqcn.isEmpty() || "null".equalsIgnoreCase(fqcn)) {
      log.warn(
          "Skipping external model '{}' because configured FQCN is empty or 'null'", modelName);
      return null;
    }

    if (!fqcn.contains(".")) {
      log.warn(
          "Skipping external model '{}' because value '{}' does not appear to be a fully-qualified"
              + " class name",
          modelName,
          fqcn);
      return null;
    }

    return fqcn;
  }

  /**
   * @return true if model is externally provided
   */
  public boolean isExternal(String modelName) {
    return externalModels.containsKey(modelName);
  }

  /**
   * @return fully-qualified class name or null
   */
  public String getFqcn(String modelName) {
    return externalModels.get(modelName);
  }
}
