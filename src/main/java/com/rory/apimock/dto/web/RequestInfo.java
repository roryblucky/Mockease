package com.rory.apimock.dto.web;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class RequestInfo implements Serializable {

    @NotEmpty
    private String path;

    @NotEmpty
    private String method;

    private boolean validationEnabled;

    private String requestSchema;

    @NotEmpty
    private Map<String, String> headers;

    private String body;

    private boolean dynamicBody;


    @JsonAnySetter
    public void addHeader(String name, String value) {
        this.headers = (this.headers != null ? this.headers : new LinkedHashMap<>());
        this.headers.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, String> getRequestHeaders() {
        return this.headers;
    }





}
