package io.github.blueprintplatform.openapi.generics.server.core.schema.enrichment;

import io.swagger.v3.oas.models.media.Schema;

/**
 * Metadata resolved from a container schema and applied to the projected wrapper schema.
 *
 * @param wrapper wrapper schema receiving vendor extensions
 * @param containerName semantic container name
 * @param containerTypeName fully-qualified Java container type
 * @param itemName contained item schema name
 */
public record ContainerSchemaMetadata(
    Schema<?> wrapper, String containerName, String containerTypeName, String itemName) {}
