package com.rory.apimock.verticles;

import com.rory.apimock.dto.Constants;
import com.rory.apimock.handlers.api.DynamicMockHandler;
import com.rory.apimock.utils.RouteBuilder;
import com.rory.apimock.utils.RouterBuilder;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockVerticle extends BaseVerticle {

    private DynamicMockHandler dynamicMockHandler;

    private Router mockSubRouter;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.mockSubRouter = Router.router(vertx);
        this.dynamicMockHandler = new DynamicMockHandler(mockSubRouter);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        this.initConsumers();

        this.createAndStartHttpServer(this.configRouter(),
                (int) JsonPointer.from("/http/mock/server/port").queryJsonOrDefault(config(), 8081))
            .onSuccess(server -> {
                log.info("Mock Server started at {}", server.actualPort());
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }

    private void initConsumers() {
        vertx.eventBus().consumer(Constants.API_MOCK_CREATE_ADDRESS).handler(dynamicMockHandler::createMockService);
        vertx.eventBus().consumer(Constants.API_MOCK_UPDATE_ADDRESS).handler(dynamicMockHandler::createMockService);
        vertx.eventBus().consumer(Constants.API_MOCK_DELETE_ADDRESS).handler(dynamicMockHandler::createMockService);
        vertx.eventBus().consumer(Constants.API_MOCK_CLEAR_ADDRESS).handler(dynamicMockHandler::createMockService);
    }

    private Router configRouter() {
        final Router mockRouter = RouterBuilder.getInstance()
            .router(vertx).build();

        RouteBuilder.getInstance(mockRouter.route("/mock/*")).commonHandler()
            .mountSubRouter(mockSubRouter).build();

        return mockRouter;
    }

}
