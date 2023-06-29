package com.rory.apimock.exceptions;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Data;


public abstract class ErrorException extends RuntimeException {

    protected ProblemDetails body;

    public ErrorException() {}
    public ErrorException(Throwable ex) {
        super(ex.getMessage(), ex);
    }

    public abstract ProblemDetails getBody(String path);
}
