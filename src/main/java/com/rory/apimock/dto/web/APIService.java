package com.rory.apimock.dto.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class APIService extends BaseDto {

    @NotEmpty
    @Size(max = 200)
    private String name;

    @NotEmpty
    private String categoryId;

    private String categoryName;

    @Size(max = 4000)
    private String description;

    @NotEmpty
    @Size(max = 1000)
    private String prefix;

    @NotEmpty
    @Size(max = 64)
    private String version;

    @Valid
    private List<APIPathDefinition> pathStubs;

    public APIService() {
    }

    public APIService(String id, String createAt, String updateAt, String name, String categoryId, String categoryName,
                       String description, String prefix, String version) {
        super(id, createAt, updateAt);
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.prefix = prefix;
        this.version = version;
    }

    public APIService(String id, String createAt, String updateAt, String name, String categoryId, String prefix, String version) {
        super(id, createAt, updateAt);
        this.name = name;
        this.categoryId = categoryId;
        this.prefix = prefix;
        this.version = version;
    }

    public APIService(String id, String createAt, String updateAt, String name, String categoryId,
                      String description, String prefix, String version, List<APIPathDefinition> pathStubs) {
        super(id, createAt, updateAt);
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
        this.prefix = prefix;
        this.version = version;
        this.pathStubs = pathStubs;
    }
}
