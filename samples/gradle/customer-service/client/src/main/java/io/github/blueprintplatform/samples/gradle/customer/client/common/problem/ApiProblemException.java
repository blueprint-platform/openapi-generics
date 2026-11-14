package io.github.blueprintplatform.samples.gradle.customer.client.common.problem;

/** Stable client error boundary; no generated HTTP exception types escape to consumers. */
public final class ApiProblemException extends RuntimeException {
  private final int status;
  private final String title;
  private final String detail;

  public ApiProblemException(int status, String title, String detail) {
    this(status, title, detail, null);
  }

  public ApiProblemException(int status, String title, String detail, Throwable cause) {
    super("HTTP " + status + ": " + detail, cause);
    this.status = status;
    this.title = title;
    this.detail = detail;
  }

  public int getStatus() {
    return status;
  }

  public String getTitle() {
    return title;
  }

  public String getDetail() {
    return detail;
  }
}
