package com.rory.apimock.exceptions;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DuplicateMockRouteException extends ErrorException {

    public DuplicateMockRouteException(String operationId) {
        super(String.format("Duplicated Route [%s] in current service", operationId));
    }

    @Override
    public ProblemDetails getBody(String path) {
        return new ProblemDetails(HttpResponseStatus.BAD_REQUEST, path, getMessage());
    }
}
