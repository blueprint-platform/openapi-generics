package io.github.blueprintplatform.samples.typecoverage.openapi;

public final class OpenApiConstants {

  public static final String TITLE = "BYOE Response Type Coverage API";

  public static final String DESCRIPTION =
          """
          Type coverage sample validating OpenAPI projection, generated client reconstruction,
          and runtime deserialization for custom response envelopes.
    
          Verified response shapes include:
    
          - ApiResponse<T>
          - ApiResponse<List<T>>
          - ApiResponse<Set<T>>
          - ApiResponse<Page<T>>
          - ApiResponse<Paging<T>>
          - ApiResponse<Window<T>>
    
          Demonstrates Bring Your Own Envelope (BYOE) and Bring Your Own Container (BYOC)
          support by reconstructing both built-in and application-defined generic response
          contracts across OpenAPI projection, generated clients, and runtime deserialization.
          """;

  public static final String SERVER_DESCRIPTION =
          "Local BYOE/BYOC response type coverage producer";

  private OpenApiConstants() {}
}