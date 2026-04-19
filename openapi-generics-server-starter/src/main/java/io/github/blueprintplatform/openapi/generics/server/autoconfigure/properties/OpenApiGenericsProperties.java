package io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "openapi-generics")
public record OpenApiGenericsProperties(@Valid EnvelopeProperties envelope) {}
