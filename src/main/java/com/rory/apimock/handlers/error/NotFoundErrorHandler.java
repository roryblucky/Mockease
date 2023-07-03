package com.rory.apimock.handlers.error;

import com.rory.apimock.dto.web.ProblemDetails;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class NotFoundErrorHandler implements Handler<RoutingContext> {


    @Override
    public void handle(RoutingContext ctx) {
        ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json");
        ctx.json(new ProblemDetails(HttpResponseStatus.NOT_FOUND, ctx.normalizedPath(), "Resource Not Found"));
    }
}
