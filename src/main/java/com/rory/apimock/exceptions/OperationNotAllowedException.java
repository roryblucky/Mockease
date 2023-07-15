package com.rory.apimock.exceptions;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;

public class OperationNotAllowedException extends ErrorException {

    public OperationNotAllowedException(String message) {
        super(String.format("Operation Not Allowed, %s.", message));
    }

    @Override
    public ProblemDetails getBody(String path) {
        return new ProblemDetails(HttpResponseStatus.FORBIDDEN, path, getMessage());
    }
}
