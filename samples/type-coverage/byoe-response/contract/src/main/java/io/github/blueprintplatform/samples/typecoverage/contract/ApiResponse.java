package io.github.blueprintplatform.samples.typecoverage.contract;

import java.util.List;
import java.util.Objects;

public class ApiResponse<T> {

  private int status;
  private String message;
  private T data;
  private List<ApiError> errors;

  public ApiResponse() {
    this.status = 200;
    this.message = "OK";
    this.errors = List.of();
  }

  public ApiResponse(int status, String message, T data, List<ApiError> errors) {
    this.status = status;
    this.message = message;
    this.data = data;
    this.errors = errors != null ? List.copyOf(errors) : List.of();
  }

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(200, "OK", data, List.of());
  }

  public static <T> ApiResponse<T> of(int status, String message, T data) {
    return new ApiResponse<>(status, message, data, List.of());
  }

  public static <T> ApiResponse<T> error(int status, String message) {
    return new ApiResponse<>(status, message, null, List.of());
  }

  public static <T> ApiResponse<T> error(int status, String message, List<ApiError> errors) {
    return new ApiResponse<>(status, message, null, errors);
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public List<ApiError> getErrors() {
    return errors;
  }

  public void setErrors(List<ApiError> errors) {
    this.errors = errors != null ? List.copyOf(errors) : List.of();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ApiResponse<?> that)) return false;
    return status == that.status
        && Objects.equals(message, that.message)
        && Objects.equals(data, that.data)
        && Objects.equals(errors, that.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, message, data, errors);
  }

  @Override
  public String toString() {
    return "ApiResponse{"
        + "status="
        + status
        + ", message='"
        + message
        + '\''
        + ", data="
        + data
        + ", errors="
        + errors
        + '}';
  }
}
