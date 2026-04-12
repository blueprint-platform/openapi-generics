package consumer.common.mapper;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.samples.customerservice.client.common.problem.ApiProblemException;
import io.github.blueprintplatform.samples.customerservice.consumer.common.exception.CustomerConsumerException;
import io.github.blueprintplatform.samples.customerservice.consumer.common.mapper.CustomerConsumerExceptionMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@Tag("unit")
@DisplayName("Unit Test: CustomerConsumerExceptionMapper")
class CustomerConsumerExceptionMapperTest {

  private final CustomerConsumerExceptionMapper mapper = new CustomerConsumerExceptionMapper();

  @Test
  @DisplayName("from -> maps status, errorCode and message correctly")
  void shouldMapAllFields() {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Bad request detail");
    problem.setProperty("errorCode", "VALIDATION_FAILED");

    ApiProblemException source = new ApiProblemException(problem, HttpStatus.BAD_REQUEST.value());

    CustomerConsumerException result = mapper.from(source);

    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    assertEquals("VALIDATION_FAILED", result.getErrorCode());
    assertEquals("Bad request detail", result.getMessage());
    assertSame(source, result.getCause());
  }

  @Test
  @DisplayName("from -> uses fallback errorCode when missing")
  void shouldUseFallbackErrorCode() {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Bad request detail");

    ApiProblemException source = new ApiProblemException(problem, HttpStatus.BAD_REQUEST.value());

    CustomerConsumerException result = mapper.from(source);

    assertEquals("UPSTREAM_ERROR", result.getErrorCode());
  }

  @Test
  @DisplayName("from -> uses fallback message when problem is null")
  void shouldUseFallbackMessageWhenProblemNull() {
    ApiProblemException source = new ApiProblemException(null, HttpStatus.BAD_REQUEST.value());

    CustomerConsumerException result = mapper.from(source);

    assertEquals("Upstream service returned an error.", result.getMessage());
  }

  @Test
  @DisplayName("from -> uses fallback message when detail is blank")
  void shouldUseFallbackMessageWhenDetailBlank() {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setDetail("");
    problem.setProperty("errorCode", "ERR");

    ApiProblemException source = new ApiProblemException(problem, HttpStatus.BAD_REQUEST.value());

    CustomerConsumerException result = mapper.from(source);

    assertEquals("Upstream service returned an error.", result.getMessage());
  }

  @Test
  @DisplayName("from -> uses INTERNAL_SERVER_ERROR when status invalid")
  void shouldFallbackToInternalServerError() {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setDetail("Any detail");
    problem.setProperties(Map.of("errorCode", "ERR"));

    ApiProblemException source = new ApiProblemException(problem, 999);

    CustomerConsumerException result = mapper.from(source);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
  }
}
