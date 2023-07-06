package com.rory.apimock.verticles;

import com.rory.apimock.dto.APIStub;
import com.rory.apimock.dto.Constants;
import com.rory.apimock.handlers.api.DynamicMockHandler;
import com.rory.apimock.handlers.error.MethodNotAllowedHandler;
import com.rory.apimock.handlers.error.NotFoundErrorHandler;
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
        this.dynamicMockHandler = new DynamicMockHandler(vertx, mockSubRouter);
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
        vertx.eventBus().<APIStub>consumer(Constants.API_PATH_STUB_CREATE_ADDRESS).handler(dynamicMockHandler::createPathStubRoute);
        vertx.eventBus().<APIStub>consumer(Constants.API_PATH_STUB_UPDATE_ADDRESS).handler(dynamicMockHandler::updatePathStubRoute);
        vertx.eventBus().<APIStub>consumer(Constants.API_PATH_STUB_DELETE_ADDRESS).handler(dynamicMockHandler::removePathStubRoute);
    }

    private Router configRouter() {
        final Router mockRouter = RouterBuilder.getInstance()
            .router(vertx).build();

        mockRouter.errorHandler(405, new MethodNotAllowedHandler());
        mockRouter.errorHandler(404, new NotFoundErrorHandler());
        RouteBuilder.getInstance(mockRouter.route("/mock/*")).commonHandler()
            .mountSubRouter(mockSubRouter).build();

        return mockRouter;
    }

}
