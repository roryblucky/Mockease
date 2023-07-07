package com.rory.apimock.dto.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class RequestInfo implements Serializable {

    @NotEmpty
    @Size(max = 1000)
    private String path;

    @NotEmpty
    @Size(max = 64)
    private String method;

    @NotEmpty
    private String contentType;
}
