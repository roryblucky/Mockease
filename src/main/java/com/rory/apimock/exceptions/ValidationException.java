package com.rory.apimock.exceptions;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

public class ValidationException extends ErrorException {

    private List<ProblemDetails.ErrorDetail> errorDetails;


    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, List<ProblemDetails.ErrorDetail> errorDetails) {
        this(message);
        this.errorDetails = errorDetails;
    }

    public ProblemDetails getBody(String path) {
        return new ProblemDetails(HttpResponseStatus.BAD_REQUEST, path, getMessage(), errorDetails);
    }
}
