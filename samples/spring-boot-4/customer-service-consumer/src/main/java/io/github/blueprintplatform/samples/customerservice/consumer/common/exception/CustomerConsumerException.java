package io.github.blueprintplatform.samples.customerservice.consumer.common.exception;

import io.github.blueprintplatform.openapi.generics.contract.error.ErrorItem;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Consumer-level exception wrapping upstream failures. Shields callers from client internals and
 * carries normalized error context for handler and logging.
 */
public final class CustomerConsumerException extends RuntimeException implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private final HttpStatus status;
  private final String errorCode;
  private final transient List<ErrorItem> errors;

  public CustomerConsumerException(
      HttpStatus status, String errorCode, String message, List<ErrorItem> errors) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
    this.errors = errors != null ? List.copyOf(errors) : List.of();
  }

  public CustomerConsumerException(
      HttpStatus status,
      String errorCode,
      String message,
      List<ErrorItem> errors,
      Throwable cause) {
    super(message, cause);
    this.status = status;
    this.errorCode = errorCode;
    this.errors = errors != null ? List.copyOf(errors) : List.of();
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public List<ErrorItem> getErrors() {
    return errors;
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }
}
