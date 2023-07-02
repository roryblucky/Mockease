package com.rory.apimock.dto.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class RequestInfo implements Serializable {

    @NotEmpty
    @Size(max = 1000)
    private String path;

    @NotEmpty
    @Size(max = 64)
    private String method;

    private boolean validationEnabled;

    private String schema;

    @NotEmpty
    private Map<String, Object> headers;

    private String body;

    private boolean dynamicBodyEnabled;

}
