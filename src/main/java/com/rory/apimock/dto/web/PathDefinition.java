package com.rory.apimock.dto.web;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class PathDefinition implements Serializable {
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
