package com.rory.apimock.handlers.mock;

import com.rory.apimock.dto.APIStub;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.util.function.Predicate;

public class MockRouterHelper {

    private static final String IDENTIFIER = "identifier";

    public Router router;


    public MockRouterHelper(Router router) {
        this.router = router;
    }

    public void removeAllRoutesOnService(String serviceId) {
        router.getRoutes().stream()
            .filter(route -> route.<String>getMetadata(IDENTIFIER).contains(serviceId))
            .forEach(Route::remove);
    }

    public void removeRoute(APIStub apiStub) {
        router.getRoutes().stream()
            .filter(unique(apiStub.getIdentifier()))
            .forEach(Route::remove);
    }

    private Predicate<Route> unique(String identifier) {
        return route -> route.getMetadata(IDENTIFIER).equals(identifier);
    }

    public Route createRoute(APIStub apiStub) {
        Route newRoute = router.route(HttpMethod.valueOf(apiStub.getMethod()), apiStub.getWholeUrl());
        //put metadata into route
        newRoute.putMetadata("identifier", apiStub.getIdentifier());
        //setting handler;
        newRoute.handler(routingContext -> {
            routingContext.response().setStatusCode(apiStub.getResponseHttpStatus());
            routingContext.response().headers().addAll(apiStub.getResponseHeaders());
            routingContext.response().end(apiStub.getResponseBody());
        });
        return newRoute;
    }
}
