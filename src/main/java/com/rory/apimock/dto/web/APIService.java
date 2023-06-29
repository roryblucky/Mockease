package com.rory.apimock.dto.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class APIService extends BaseDto {

    @NotEmpty
    private String name;

    @NotNull
    private String categoryId;

    private String categoryName;

    private String description;

    @NotEmpty
    private String prefix;

    @NotEmpty
    private String version;

    private List<APIPathDefinition> pathStubs;
}
