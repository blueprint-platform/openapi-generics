package io.github.blueprintplatform.openapi.generics.gradle.exception;

/** Raised when generated Java sources cannot be normalized deterministically. */
// Preserve the shared Gradle exception contract and its Gradle/JDK ancestry.
@SuppressWarnings("java:S110")
public final class GeneratedSourceNormalizationException
        extends OpenApiGenericsGradleException {

    public GeneratedSourceNormalizationException(String message) {
        super(message);
    }

    public GeneratedSourceNormalizationException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
