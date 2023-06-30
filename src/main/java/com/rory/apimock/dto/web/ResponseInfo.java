package com.rory.apimock.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
public class ResponseInfo implements Serializable {

    @NotEmpty
    @Size(max = 64)
    private String httpStatus;
    private Map<String, Object> headers;
    private boolean dynamicBody;
    @NotEmpty
    private String body;
    // response delay?
    // Fault mock?
    @Valid
    private WebhookInfo webhook;

    @Valid
    private Proxy proxy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookInfo {

        @JsonIgnore
        private String id;

        @NotEmpty
        @Size(max = 1000)
        private String url;
        @NotEmpty
        @Size(max = 64)
        private String method;

        private boolean dynamicBody;

        private Map<String, Object> headers;

        @NotEmpty
        private String body;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Proxy {

        @JsonIgnore
        private String id;

        @NotEmpty
        @Size(max = 64)
        private String url;
    }
}
