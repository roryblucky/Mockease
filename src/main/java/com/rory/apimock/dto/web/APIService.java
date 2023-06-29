package com.rory.apimock.dto.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class APIService extends BaseDto {

    @NotEmpty
    private String name;

    @NotNull
    private String categoryId;

    private String categoryName;

    private String categoryIdentifier;

    private String description;

    @NotEmpty
    private String prefix;

    @NotEmpty
    private String version;
}
