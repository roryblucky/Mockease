package com.rory.apimock.dto.web;

import lombok.Data;

import java.io.Serializable;

@Data
public abstract class BaseDto implements Serializable {
    protected BaseDto() {}

    public BaseDto(String id, String createAt, String updateAt) {
        this.id = id;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    protected String id;

    protected String createAt;

    protected String updateAt;
}
