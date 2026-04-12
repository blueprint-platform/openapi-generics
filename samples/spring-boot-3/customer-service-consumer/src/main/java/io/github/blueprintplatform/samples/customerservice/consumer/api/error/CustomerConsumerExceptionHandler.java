package io.github.blueprintplatform.samples.customerservice.consumer.api.error;

import io.github.blueprintplatform.samples.customerservice.consumer.common.exception.CustomerConsumerException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Handles consumer-level exceptions and produces RFC 9457 problem responses. Only consumer-owned
 * exception types are handled here.
 */
@RestControllerAdvice
@Order(1)
public class CustomerConsumerExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(CustomerConsumerExceptionHandler.class);

  private static final String KEY_ERROR_CODE = "errorCode";
  private static final String PROBLEM_BASE = "urn:customer-service-consumer:problem:";
  private static final String ERROR_CODE_INTERNAL_ERROR = "INTERNAL_ERROR";

  @ExceptionHandler(CustomerConsumerException.class)
  public ProblemDetail handleCustomerConsumerException(
      CustomerConsumerException ex, HttpServletRequest req) {

    log.warn("Consumer error [status={}, code={}]", ex.getStatus(), ex.getErrorCode());

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());

    pd.setType(URI.create(PROBLEM_BASE + "upstream-error"));
    pd.setTitle("Upstream Error");
    pd.setInstance(instance(req));
    pd.setProperty(KEY_ERROR_CODE, ex.getErrorCode());

    return pd;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception", ex);

    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");

    pd.setType(URI.create(PROBLEM_BASE + "internal-error"));
    pd.setTitle("Internal Server Error");
    pd.setInstance(instance(req));
    pd.setProperty(KEY_ERROR_CODE, ERROR_CODE_INTERNAL_ERROR);

    return pd;
  }

  private URI instance(HttpServletRequest req) {
    return UriComponentsBuilder.fromPath(req.getRequestURI()).build().toUri();
  }
}
