package com.rory.apimock.handlers.error;

import com.rory.apimock.dto.web.ProblemDetails;
import com.rory.apimock.exceptions.ErrorException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class APIFailureHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext ctx) {
        Throwable failure = ctx.failure();
        ProblemDetails body = null;
        if (failure instanceof ErrorException) {
            ErrorException errorException = (ErrorException) failure;
            body = errorException.getBody(ctx.normalizedPath());
        } else {
            log.error("Unexpected error: ", failure);
            body = new ProblemDetails(HttpResponseStatus.INTERNAL_SERVER_ERROR, ctx.normalizedPath(), "Internal server error");
        }
        ctx.response().setStatusCode(body.getStatus());
        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json");
        ctx.json(body);
    }
}
