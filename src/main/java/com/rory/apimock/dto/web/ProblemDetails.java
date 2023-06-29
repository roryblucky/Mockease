package com.rory.apimock.dto.web;

import com.rory.apimock.utils.DateUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ProblemDetails {
    private String type;
    private String title;
    private int status;
    private String detail;
    private List<ErrorDetail> errors;
    private String instance;
    private String errorDateTime = DateUtil.getDateTimeString();

    private ProblemDetails(String type, String title, int status, String detail, List<ErrorDetail> errors, String instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.errors = errors;
        this.instance = instance;
    }

    public ProblemDetails(HttpResponseStatus httpResponseStatus, String instance, String detail) {
        this(httpResponseStatus, instance, detail, null);
    }

    public ProblemDetails(HttpResponseStatus httpResponseStatus, String instance, String detail, List<ErrorDetail> errors) {
        this("about:blank", httpResponseStatus.reasonPhrase(), httpResponseStatus.code(), detail, errors, instance);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private String type;
        private String detail;
    }


}
