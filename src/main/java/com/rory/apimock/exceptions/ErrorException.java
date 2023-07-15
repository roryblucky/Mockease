package com.rory.apimock.exceptions;

import com.rory.apimock.dto.web.ProblemDetails;


public abstract class ErrorException extends RuntimeException {

    protected ProblemDetails body;

    public ErrorException() {}
    public ErrorException(Throwable ex) {
        super(ex.getMessage(), ex);
    }
    public ErrorException(String message) {
        super(message);
    }


    public abstract ProblemDetails getBody(String path);
}
