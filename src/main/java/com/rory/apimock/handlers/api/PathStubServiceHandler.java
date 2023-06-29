package com.rory.apimock.handlers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rory.apimock.dto.Constants;
import com.rory.apimock.dto.web.APIPathDefinition;
import com.rory.apimock.dto.web.RequestWrapper;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PathStubServiceHandler {

    private final Vertx vertx;

    public PathStubServiceHandler(Vertx vertx) {
        this.vertx = vertx;
    }

    public void createPathStub(RoutingContext ctx) {
        RequestWrapper<APIPathDefinition> request = JacksonCodec.decodeValue(ctx.body().buffer(), new TypeReference<>() {
        });
        APIPathDefinition apiPathDefinition = request.getData();
        vertx.eventBus().request(Constants.API_MOCK_CREATE_ADDRESS, apiPathDefinition)
            .onSuccess(message -> {
                log.info("create Mock endpoint success, msg -> {}", message.body());
                ctx.json(RequestWrapper.of(apiPathDefinition));
            })
            .onFailure(ctx::fail);
    }

    public void getPaths(RoutingContext ctx) {
    }

    public void updatePathStub(RoutingContext ctx) {
    }

    public void deletePathStub(RoutingContext ctx) {
    }
}
