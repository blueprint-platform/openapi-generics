package io.github.blueprintplatform.samples.customerservice.consumer.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerConsumerUpdateRequest(
    @NotBlank @Size(min = 2, max = 80) String name, @NotBlank @Email String email) {}
