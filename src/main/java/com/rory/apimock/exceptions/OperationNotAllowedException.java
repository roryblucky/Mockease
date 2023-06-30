package com.rory.apimock.exceptions;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;

public class OperationNotAllowedException extends ErrorException {

    private final String message;

    public OperationNotAllowedException(String message) {
        this.message = message;
    }

    @Override
    public ProblemDetails getBody(String path) {
        return new ProblemDetails(HttpResponseStatus.FORBIDDEN, path, String.format("Operation Not Allowed, %s.", this.message));
    }
}
