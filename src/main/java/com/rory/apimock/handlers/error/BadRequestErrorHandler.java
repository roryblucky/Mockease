package com.rory.apimock.handlers.error;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class BadRequestErrorHandler implements Handler<RoutingContext> {


    @Override
    public void handle(RoutingContext ctx) {
        ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json");
        ctx.json(new ProblemDetails(HttpResponseStatus.BAD_REQUEST, ctx.normalizedPath(), "API Request is not matched"));
    }
}
