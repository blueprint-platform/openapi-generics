package io.github.blueprintplatform.samples.customerservice.consumer.api.dto;

import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
public record CustomerConsumerSearchCriteria(String name, String email) {}
