package io.github.blueprintplatform.openapi.generics.gradle.exception;

/** Raised when generated Java sources cannot be normalized deterministically. */
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
