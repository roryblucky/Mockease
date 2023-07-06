package com.rory.apimock.exceptions;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DuplicateMockRouteException extends ErrorException {

    private final String url;

    public DuplicateMockRouteException(String url) {
        this.url = url;
    }

    @Override
    public ProblemDetails getBody(String path) {
        return new ProblemDetails(HttpResponseStatus.BAD_REQUEST, path, String.format("Duplicated Route [%s] in current service", this.url));
    }
}
