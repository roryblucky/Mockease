package com.rory.apimock.dto.web;

import com.rory.apimock.dao.BaseDao;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class APICategory extends BaseDto {
    public APICategory() {
    }

    public APICategory(String id, String createAt, String updateAt, String name, String description) {
        super(id, createAt, updateAt);
        this.name = name;
        this.description = description;
    }

    @NotEmpty
    @Size(max = 200)
    private String name;

    @Size(max = 4000)
    private String description;

}
