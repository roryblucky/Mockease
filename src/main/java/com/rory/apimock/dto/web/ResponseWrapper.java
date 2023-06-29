package com.rory.apimock.dto.web;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseWrapper<T> {

    @NotNull
    @Valid
    private T data;

    private Link links;



    private static <R> ResponseWrapper<R> of(R data) {
        return new ResponseWrapper<>(data, null);
    }

    private static <R> ResponseWrapper<R> of(R data, String selfUrl) {
        return new ResponseWrapper<>(data, new Link(new Self(selfUrl.replaceAll("//", "/"))));
    }

    public static <R extends BaseDto> ResponseWrapper<R> create(RoutingContext ctx, R data) {
        ctx.response().setStatusCode(HttpResponseStatus.CREATED.code());
        return of(data, ctx.normalizedPath() + "/" + data.getId());
    }

    public static <R> ResponseWrapper<R> ok(RoutingContext ctx, R data) {
        ctx.response().setStatusCode(HttpResponseStatus.OK.code());
        return of(data);
    }

    public static <R> ResponseWrapper<R> noContent(RoutingContext ctx) {
        ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
        return of(null);
    }


    public static class Metadata {
        private int totalsItems;
    }

    @Data
    @Builder
    public static class Link {
        private Self self;
    }

    @Data
    @Builder
    public static class Self {
        private String href;
    }

}
