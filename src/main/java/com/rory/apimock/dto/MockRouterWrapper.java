package com.rory.apimock.dto;

import io.vertx.core.shareddata.Shareable;
import io.vertx.ext.web.Router;

public class MockRouterWrapper implements Shareable {
    private final Router router;

    public MockRouterWrapper(Router router) {
        this.router = router;
    }

    public static MockRouterWrapper of(Router router) {
        return new MockRouterWrapper(router);
    }

    public Router getRouter() {
        return router;
    }
}
