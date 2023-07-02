package com.rory.apimock.dto.web;

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

    @NotEmpty
    private Map<String, Object> headers;

    private boolean dynamicBodyEnabled;

    @NotEmpty
    private String body;
    // response delay?
    // Fault mock?

    private boolean webhookEnabled;
    private boolean proxyEnabled;

    @Valid
    private WebhookInfo webhook;

    @Valid
    private Proxy proxy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookInfo {


        @NotEmpty
        @Size(max = 1000)
        private String url;
        @NotEmpty
        @Size(max = 64)
        private String method;

        private boolean dynamicBodyEnabled;

        @NotEmpty
        private Map<String, Object> headers;

        @NotEmpty
        private String body;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Proxy {

        @NotEmpty
        @Size(max = 64)
        private String url;
    }
}
