package com.rory.apimock.handlers.api;

import com.rory.apimock.dto.APIStub;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

public class DynamicMockHandler {

    private final Router mockSubRouter;

    private final Vertx vertx;

    public DynamicMockHandler(Vertx vertx, Router router) {
        this.vertx = vertx;
        this.mockSubRouter = router;
    }

    public void createPathStubRoute(Message<APIStub> message) {
        APIStub apiStub = message.body();
        if (isDuplicatedRoute(apiStub)) {
            message.fail(400, String.format("%s %s", apiStub.getMethod(), apiStub.getWholeUrl()));
        } else  {
            Route newServiceRoute = mockSubRouter.route(HttpMethod.valueOf(apiStub.getMethod()), apiStub.getWholeUrl());
            //put metadata into route
            newServiceRoute.putMetadata("identifier", apiStub.getIdentifier());
            //setting handler;
            newServiceRoute.handler(routingContext -> {
                routingContext.response().setStatusCode(apiStub.getResponseHttpStatus());
                routingContext.response().headers().addAll(apiStub.getResponseHeaders());
                routingContext.response().end(apiStub.getResponseBody());
            });
            message.reply("Created");
        }
    }

    public void removePathStubRoute(Message<APIStub> message) {
        APIStub apiStub = message.body();
        apiStub.getIdentifier();
        mockSubRouter.getRoutes().stream()
            .filter(route -> route.getMetadata("identifier").equals(apiStub.getIdentifier()))
            .forEach(Route::remove);
        message.reply("Removed");
    }

    public void updatePathStubRoute(Message<APIStub> message) {
        APIStub apiStub = message.body();

    }



    private boolean isDuplicatedRoute(APIStub apiStub) {
        return mockSubRouter.getRoutes().stream()
            .anyMatch(route -> route.getMetadata("identifier").equals(apiStub.getIdentifier()));
    }

}
