package com.rory.apimock.handlers.mock.runtime;

import com.rory.apimock.dto.APIStub;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

import static com.rory.apimock.dto.Constants.API_MOCK_WEBHOOK_ADDRESS;
import static com.rory.apimock.dto.Constants.DYNAMIC_RESPONSE;

public class StaticResponseHandler implements Handler<RoutingContext> {


    private final APIStub apiStub;
    private final Vertx vertx;

    public StaticResponseHandler(Vertx vertx, APIStub apiStub) {
        this.vertx = vertx;
        this.apiStub = apiStub;
    }


    @Override
    public void handle(RoutingContext ctx) {
        if (apiStub.isWebhookEnabled()) {
            ctx.addEndHandler(v -> vertx.eventBus().send(API_MOCK_WEBHOOK_ADDRESS, apiStub));
        }
        ctx.response().setStatusCode(apiStub.getResponseHttpStatus());
        ctx.response().headers().addAll(apiStub.getResponseHeaders());
        Object dynamicResponse = ctx.get(DYNAMIC_RESPONSE);
        if (dynamicResponse != null) {
            ctx.response().end((Buffer)dynamicResponse);
        } else {
            ctx.response().end(apiStub.getResponseBody());
        }
    }
}
