package io.github.blueprintplatform.samples.customerservice.consumer.common.mapper;

import io.github.blueprintplatform.samples.customerservice.client.common.problem.ApiProblemException;
import io.github.blueprintplatform.samples.customerservice.consumer.common.exception.CustomerConsumerException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Maps ApiProblemException to CustomerConsumerException, isolating consumer layers from client
 * internals.
 */
@Component
public class CustomerConsumerExceptionMapper {

  private static final String FALLBACK_ERROR_CODE = "UPSTREAM_ERROR";
  private static final String FALLBACK_MESSAGE = "Upstream service returned an error.";

  public CustomerConsumerException from(ApiProblemException source) {
    HttpStatus status = resolveStatus(source);
    String errorCode = resolveErrorCode(source);
    String message = resolveMessage(source);

    return new CustomerConsumerException(status, errorCode, message, source.getErrors(), source);
  }

  private HttpStatus resolveStatus(ApiProblemException source) {
    HttpStatus resolved = HttpStatus.resolve(source.getStatus());
    return resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
  }

  private String resolveErrorCode(ApiProblemException source) {
    String code = source.getErrorCode();
    return (code != null && !code.isBlank()) ? code : FALLBACK_ERROR_CODE;
  }

  private String resolveMessage(ApiProblemException source) {
    if (source.getProblem() == null) {
      return FALLBACK_MESSAGE;
    }
    String detail = source.getProblem().getDetail();
    return (detail != null && !detail.isBlank()) ? detail : FALLBACK_MESSAGE;
  }
}
