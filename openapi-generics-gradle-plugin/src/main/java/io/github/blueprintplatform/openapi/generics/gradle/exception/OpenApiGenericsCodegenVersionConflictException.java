package io.github.blueprintplatform.openapi.generics.gradle.exception;

/**
 * Raised when the consumer declares an OpenAPI Generics codegen version that does not match
 * the applied OpenAPI Generics Gradle Plugin version.
 */
public final class OpenApiGenericsCodegenVersionConflictException
        extends OpenApiGenericsGradleException {

    public OpenApiGenericsCodegenVersionConflictException(
            String expectedVersion,
            String declaredVersion,
            String configurationName) {

        super(
                "OpenAPI Generics codegen dependency version conflict: expected "
                        + "'io.github.blueprint-platform:openapi-generics-java-codegen:"
                        + expectedVersion
                        + "' but the consumer declared version '"
                        + declaredVersion
                        + "' in configuration '"
                        + configurationName
                        + "'. Remove the explicit dependency and let the plugin provision "
                        + "the matching codegen artifact.");
    }
}
