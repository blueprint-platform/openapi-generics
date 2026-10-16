package io.github.blueprintplatform.openapi.generics.gradle.dependency;

import io.github.blueprintplatform.openapi.generics.gradle.exception.OpenApiGenericsCodegenVersionConflictException;
import io.github.blueprintplatform.openapi.generics.gradle.exception.PluginVersionResolutionException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

/**
 * Registers and exposes the OpenAPI Generics custom codegen artifact.
 */
public final class OpenApiGenericsCodegenClasspath {

    public static final String OPENAPI_GENERICS_GROUP =
            "io.github.blueprint-platform";

    public static final String OPENAPI_GENERICS_CODEGEN_MODULE =
            "openapi-generics-java-codegen";

    private final Class<?> pluginImplementationType;

    public OpenApiGenericsCodegenClasspath(
            Class<?> pluginImplementationType) {

        this.pluginImplementationType = pluginImplementationType;
    }

    public Dependency register(
            Project project,
            Configuration generatorExtraClasspath) {

        String pluginVersion = resolvePluginVersion();

        /*
         * Validate both dependencies already present on the configuration and
         * dependencies added later by the consumer build script.
         *
         * The plugin is applied before the consumer's dependencies block is
         * normally evaluated, so a one-time lookup here would miss later
         * conflicting declarations.
         */
        generatorExtraClasspath
                .getDependencies()
                .all(
                        dependency ->
                                validateCodegenDependency(
                                        dependency,
                                        pluginVersion,
                                        generatorExtraClasspath.getName()));

        Dependency existingDependency =
                generatorExtraClasspath
                        .getDependencies()
                        .stream()
                        .filter(this::isOpenApiGenericsCodegenDependency)
                        .findFirst()
                        .orElse(null);

        if (existingDependency != null) {
            return existingDependency;
        }

        return project.getDependencies()
                .add(
                        generatorExtraClasspath.getName(),
                        "%s:%s:%s"
                                .formatted(
                                        OPENAPI_GENERICS_GROUP,
                                        OPENAPI_GENERICS_CODEGEN_MODULE,
                                        pluginVersion));
    }

    private boolean isOpenApiGenericsCodegenDependency(
            Dependency dependency) {

        return OPENAPI_GENERICS_GROUP.equals(
                dependency.getGroup())
                && OPENAPI_GENERICS_CODEGEN_MODULE.equals(
                dependency.getName());
    }

    private void validateCodegenDependency(
            Dependency dependency,
            String pluginVersion,
            String configurationName) {

        if (!isOpenApiGenericsCodegenDependency(dependency)) {
            return;
        }

        if (pluginVersion.equals(dependency.getVersion())) {
            return;
        }

        throw new OpenApiGenericsCodegenVersionConflictException(
                pluginVersion,
                dependency.getVersion(),
                configurationName);
    }

    private String resolvePluginVersion() {
        String implementationVersion =
                pluginImplementationType
                        .getPackage()
                        .getImplementationVersion();

        if (implementationVersion == null
                || implementationVersion.isBlank()) {

            throw new PluginVersionResolutionException(
                    "OpenAPI Generics Gradle Plugin",
                    pluginImplementationType);
        }

        return implementationVersion;
    }
}