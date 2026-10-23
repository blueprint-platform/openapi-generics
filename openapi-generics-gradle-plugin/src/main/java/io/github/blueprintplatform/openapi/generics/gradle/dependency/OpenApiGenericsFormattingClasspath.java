package io.github.blueprintplatform.openapi.generics.gradle.dependency;

import com.diffplug.spotless.java.CleanthatJavaStep;
import com.diffplug.spotless.java.GoogleJavaFormatStep;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

/** Provisions the formatter runtime used by generated-source normalization. */
public final class OpenApiGenericsFormattingClasspath {

    public static final String CONFIGURATION_NAME = "openApiGenericsFormatter";
    public static final String SPOTLESS_LIB_VERSION = "4.10.4";

    private static final String SPOTLESS_LIB_COORDINATE =
            "com.diffplug.spotless:spotless-lib:" + SPOTLESS_LIB_VERSION;

    public Configuration register(Project project) {
        Configuration configuration =
                project.getConfigurations().maybeCreate(CONFIGURATION_NAME);

        configuration.setCanBeResolved(true);
        configuration.setCanBeConsumed(false);
        configuration.setVisible(false);
        configuration.setDescription(
                "OpenAPI Generics embedded generated-source formatter runtime.");

        project.getDependencies().add(CONFIGURATION_NAME, SPOTLESS_LIB_COORDINATE);
        project.getDependencies()
                .add(
                        CONFIGURATION_NAME,
                        CleanthatJavaStep.defaultGroupArtifact()
                                + ":"
                                + CleanthatJavaStep.defaultVersion());

        String javaParserVersion = CleanthatJavaStep.defaultJavaparserVersion();
        if (javaParserVersion != null) {
            project.getDependencies()
                    .add(
                            CONFIGURATION_NAME,
                            "com.github.javaparser:javaparser-symbol-solver-core:"
                                    + javaParserVersion);
        }

        project.getDependencies()
                .add(
                        CONFIGURATION_NAME,
                        GoogleJavaFormatStep.defaultGroupArtifact()
                                + ":"
                                + GoogleJavaFormatStep.defaultVersion());

        return configuration;
    }
}
