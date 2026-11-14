package io.github.blueprintplatform.samples.gradle.customer.consumer.api.error;

import io.github.blueprintplatform.samples.gradle.customer.client.common.problem.ApiProblemException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomerConsumerExceptionHandler {
  @ExceptionHandler(ApiProblemException.class)
  public ProblemDetail handleApiProblem(ApiProblemException exception) {
    var problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatusCode.valueOf(exception.getStatus()), exception.getDetail());
    problem.setTitle(exception.getTitle());
    return problem;
  }
}
