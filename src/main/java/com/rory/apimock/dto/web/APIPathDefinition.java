package com.rory.apimock.dto.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

@Data
public class APIPathDefinition implements Serializable {
    private String id;

    @NotEmpty
    private String name;
    @NotEmpty
    private String operationId;

    @Valid
    private RequestInfo request;

    @Valid
    private ResponseInfo response;

}
