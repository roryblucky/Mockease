package com.rory.apimock.exceptions;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;

public class ResourceNotFoundException extends ErrorException {

    public ResourceNotFoundException() {
        super("Resource Not Found");
    }

    @Override
    public ProblemDetails getBody(String path) {
        return  new ProblemDetails(HttpResponseStatus.NOT_FOUND, path, getMessage());
    }
}
