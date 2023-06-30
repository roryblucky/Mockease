package com.rory.apimock.dto.web;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class APIService extends BaseDto {

    @NotEmpty
    private String name;

    @NotEmpty
    private String categoryId;

    private String categoryName;

    private String description;

    @NotEmpty
    private String prefix;

    @NotEmpty
    private String version;

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
