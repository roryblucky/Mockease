package com.rory.apimock.dto.web;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ResponseInfo implements Serializable {

    @NotEmpty
    private String httpStatus;
    private Map<String, String> headers;
    private boolean dynamicBody;
    @NotEmpty
    private String body;
    // response delay?
    // Fault mock?
    @Valid
    private WebhookInfo webhook;

    @Valid
    private Proxy proxy;


    @JsonAnySetter
    public void addHeader(String name, String value) {
        this.headers = (this.headers != null ? this.headers : new LinkedHashMap<>());
        this.headers.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, String> getResponseHeaders() {
        return this.headers;
    }

    @Data
    public static class WebhookInfo {
        @NotEmpty
        private String url;
        @NotEmpty
        private String method;

        private boolean dynamicBody;

        private Map<String, String> headers;

        @NotEmpty
        private String body;

        @JsonAnySetter
        public void addHeader(String name, String value) {
            this.headers = (this.headers != null ? this.headers : new LinkedHashMap<>());
            this.headers.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, String> getResponseHeaders() {
            return this.headers;
        }
    }

    @Data
    public static class Proxy {
        @NotEmpty
        private String url;
    }
}
