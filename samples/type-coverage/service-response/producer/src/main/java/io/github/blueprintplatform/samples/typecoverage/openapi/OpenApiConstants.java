package io.github.blueprintplatform.samples.typecoverage.openapi;

public final class OpenApiConstants {

  public static final String TITLE = "ServiceResponse Type Coverage API";

  public static final String DESCRIPTION =
          "Type coverage sample validating contract projection and client reconstruction for ServiceResponse<T>, ServiceResponse<List<T>>, ServiceResponse<Set<T>>, and ServiceResponse<Page<T>>.";

  public static final String SERVER_DESCRIPTION =
          "Local producer for generic response contract projection and container coverage validation.";

  private OpenApiConstants() {}
}