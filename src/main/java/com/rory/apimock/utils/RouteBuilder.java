package com.rory.apimock.utils;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;

public class RouteBuilder {
    private final Route route;

    public RouteBuilder(Route route) {
        this.route = route;
    }

    public static RouteBuilder getInstance(Route route) {
        return new RouteBuilder(route);
    }

    public RouteBuilder handler(Handler<RoutingContext> handler) {
        this.route.handler(handler);
        return this;
    }

    public RouteBuilder commonHandler() {
        this.route
            .handler(LoggerHandler.create())
            .handler(ResponseTimeHandler.create())
            .handler(ResponseContentTypeHandler.create())
            .handler(CorsHandler.create().addOrigin("*"))
            .handler(BodyHandler.create());

        return this;
    }

    public RouteBuilder mountSubRouter(Router subRouter) {
        this.route.subRouter(subRouter);
        return this;
    }
    public Route build() {
        return this.route;
    }
}
