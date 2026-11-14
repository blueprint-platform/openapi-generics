package io.github.blueprintplatform.openapi.generics.gradle.formatting;

import org.gradle.api.file.ConfigurableFileCollection;

/** Managed task-local inputs used by generated-source normalization. */
public abstract class OpenApiGenericsFormattingSpec {

    public static final String EXTENSION_NAME = "openApiGenericsFormatting";

    public abstract ConfigurableFileCollection getFormatterClasspath();

    public abstract ConfigurableFileCollection getGeneratedJavaDirectory();
}
