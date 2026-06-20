package io.github.blueprintplatform.openapi.generics.codegen.metadata;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenProperties;
import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerMetadataResolver {

    private static final Logger log = LoggerFactory.getLogger(ContainerMetadataResolver.class);

    private final Map<String, ContainerMetadata> containers = new HashMap<>();

    public ContainerMetadataResolver() {
        containers.put("List", new ContainerMetadata(List.class.getSimpleName(), List.class.getCanonicalName()));
        containers.put("Page", new ContainerMetadata(Page.class.getSimpleName(), Page.class.getCanonicalName()));
    }

    public void register(Map<String, Object> additionalProperties) {
        if (additionalProperties == null || additionalProperties.isEmpty()) {
            return;
        }

        additionalProperties.forEach(this::registerIfContainerProperty);
    }

    public void apply(CodegenModel model) {
        if (!isWrapperModel(model)) {
            return;
        }

        Map<String, Object> vendorExtensions = model.getVendorExtensions();

        if (vendorExtensions == null) {
            return;
        }

        Object rawContainer = vendorExtensions.get(CodegenVendorExtensions.DATA_CONTAINER);

        if (!(rawContainer instanceof String containerName) || containerName.isBlank()) {
            return;
        }

        ContainerMetadata metadata = containers.get(containerName);

        if (metadata == null) {
            vendorExtensions.put(CodegenVendorExtensions.DATA_CONTAINER_TYPE, containerName);
            log.debug("Using container name as type without import: {} (model: {})", containerName, model.name);
            return;
        }

        vendorExtensions.put(CodegenVendorExtensions.DATA_CONTAINER_TYPE, metadata.type());

        if (metadata.importType() != null) {
            vendorExtensions.put(CodegenVendorExtensions.DATA_CONTAINER_IMPORT, metadata.importType());
        }

        log.debug(
                "Container metadata applied to wrapper model: {} -> type={}, import={}",
                model.name,
                metadata.type(),
                metadata.importType());
    }

    private void registerIfContainerProperty(String key, Object raw) {
        if (key == null || !key.startsWith(CodegenProperties.DATA_CONTAINER_PREFIX)) {
            return;
        }

        String containerName = key.substring(CodegenProperties.DATA_CONTAINER_PREFIX.length());
        String fqcn = normalizeFqcn(containerName, raw);

        if (fqcn == null) {
            return;
        }

        containers.put(containerName, new ContainerMetadata(extractSimpleName(fqcn), fqcn));
        log.debug("Registered external container mapping: {} -> {}", containerName, fqcn);
    }

    private String normalizeFqcn(String containerName, Object raw) {
        if (raw == null) {
            log.warn("Skipping container '{}' because value is null", containerName);
            return null;
        }

        String fqcn = String.valueOf(raw).trim();

        if (fqcn.isEmpty() || "null".equalsIgnoreCase(fqcn)) {
            log.warn("Skipping container '{}' because configured FQCN is empty or 'null'", containerName);
            return null;
        }

        if (!fqcn.contains(".")) {
            log.warn(
                    "Skipping container '{}' because value '{}' does not appear to be a fully-qualified class name",
                    containerName,
                    fqcn);
            return null;
        }

        return fqcn;
    }

    private boolean isWrapperModel(CodegenModel model) {
        Map<String, Object> vendorExtensions = model.getVendorExtensions();
        return vendorExtensions != null
                && Boolean.TRUE.equals(vendorExtensions.get(CodegenVendorExtensions.API_WRAPPER));
    }

    private String extractSimpleName(String fqcn) {
        int lastDot = fqcn.lastIndexOf('.');
        return lastDot >= 0 ? fqcn.substring(lastDot + 1) : fqcn;
    }

    private record ContainerMetadata(String type, String importType) {}
}