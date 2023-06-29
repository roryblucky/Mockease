package com.rory.apimock.dto.web;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestWrapper<T> {

    @NotNull
    @Valid
    private T data;

    public static <R> RequestWrapper<R> of(R data) {
        return new RequestWrapper<>(data);
    }

}
