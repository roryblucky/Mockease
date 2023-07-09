package com.rory.apimock.handlers.mock;

import com.rory.apimock.dto.APIStub;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

@Slf4j
public class MockRouterHelper {

    private static final String IDENTIFIER = "identifier";

    private final Router router;
    private final Vertx vertx;


    public MockRouterHelper(Vertx vertx, Router router) {
        this.vertx = vertx;
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
        Route newRoute = router.route(HttpMethod.valueOf(apiStub.getMethod()), apiStub.getWholeUrl())
            .putMetadata("identifier", apiStub.getIdentifier());
        this.configContentType(apiStub, newRoute);

        if (apiStub.isProxyEnabled()) {
            newRoute.handler(new MockProxyHandler(vertx, apiStub));
        } else {
            newRoute.handler(new DynamicResponseHandler(vertx, apiStub));
            newRoute.handler(new StaticResponseHandler(vertx, apiStub));
        }

        return newRoute;
    }


    public void fireWebhook(APIStub apiStub) {
        log.info("webhook fired for: {}", apiStub.getIdentifier());
    }


    private void configContentType(APIStub apiStub, Route newRoute) {
        final String requestContentType = apiStub.getRequestContentType();
        final String responseContentType = apiStub.getResponseHeaders().get(HttpHeaders.CONTENT_TYPE.toString());
        if (requestContentType != null) {
            newRoute.consumes(requestContentType);
        }

        if (responseContentType != null) {
            newRoute.produces(responseContentType);
        }
    }
}
