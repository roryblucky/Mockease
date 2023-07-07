package com.rory.apimock.dto.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class APIPathDefinition extends BaseDto {
    private String id;

    @NotEmpty
    @Size(max = 200)
    private String name;

    @Size(max = 4000)
    private String description;

    @NotEmpty
    @Size(max = 64)
    private String operationId;

    @Valid
    @NotNull
    private RequestInfo request;

    @Valid
    @NotNull
    private ResponseInfo response;

}
