package com.rory.apimock.utils;

import com.rory.apimock.handlers.error.MethodNotAllowedHandler;
import com.rory.apimock.handlers.error.NotFoundErrorHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;

public class RouterBuilder {
    private Router router;


    public static RouterBuilder getInstance() {
        return new RouterBuilder();
    }

    public RouterBuilder router(Vertx vertx) {
        this.router = Router.router(vertx);
//        router.errorHandler(HttpResponseStatus.NOT_FOUND.code(), NotFoundErrorHandler.create());
//        router.errorHandler(HttpResponseStatus.METHOD_NOT_ALLOWED.code(), MethodNotAllowedHandler.create());
        return this;
    }

    public Router build() {
        return this.router;
    }
}

