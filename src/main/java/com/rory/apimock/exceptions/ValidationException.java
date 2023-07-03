package com.rory.apimock.exceptions;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

public class ValidationException extends ErrorException {

    private final String detail;
    private List<ProblemDetails.ErrorDetail> errorDetails;


    public ValidationException(String detail) {
        this.detail = detail;
    }

    public ValidationException(String detail, List<ProblemDetails.ErrorDetail> errorDetails) {
        this.errorDetails = errorDetails;
        this.detail = detail;
    }

    public ProblemDetails getBody(String path) {
        return new ProblemDetails(HttpResponseStatus.BAD_REQUEST, path, detail, errorDetails);
    }
}
