package com.rory.apimock.exceptions;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.validation.ConstraintViolation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationException extends ErrorException {

    private final Set<ConstraintViolation<Object>> constraintViolations;

    public ValidationException(Set<ConstraintViolation<Object>> constraintViolations) {
        this.constraintViolations = constraintViolations;
    }

    public ProblemDetails getBody(String path) {
        List<ProblemDetails.ErrorDetail> errorDetails = this.constraintViolations.stream()
            .map(error -> new ProblemDetails.ErrorDetail(error.getPropertyPath().toString(), error.getMessage()))
            .collect(Collectors.toList());
        return  new ProblemDetails(HttpResponseStatus.BAD_REQUEST, path, "Fields Validation Error", errorDetails);
    }
}
