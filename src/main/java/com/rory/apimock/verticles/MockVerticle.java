package com.rory.apimock.verticles;

import com.rory.apimock.dao.APIServiceDao;
import com.rory.apimock.dto.APIStub;
import com.rory.apimock.dto.web.APIService;
import com.rory.apimock.handlers.error.MethodNotAllowedHandler;
import com.rory.apimock.handlers.error.NotFoundErrorHandler;
import com.rory.apimock.handlers.mock.MockPathHandler;
import com.rory.apimock.utils.JsonPointerUtil;
import com.rory.apimock.utils.RouteBuilder;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

import static com.rory.apimock.dto.Constants.*;

@Slf4j
public class MockVerticle extends BaseVerticle {

    private MockPathHandler mockPathHandler;

    private Router mockSubRouter;
    private APIServiceDao apiServiceDao;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.mockSubRouter = Router.router(vertx);
        this.apiServiceDao = new APIServiceDao(sqlClient);
        this.mockPathHandler = new MockPathHandler(vertx, mockSubRouter);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        this.initEventBusConsumers();

        this.configRouter().compose(router ->
                this.createAndStartHttpServer(router,
                    JsonPointerUtil.queryJsonOrDefault("/http/mock/server/host", config(), 8081)))
            .onSuccess(server -> {
                log.info("Mock Server started at {}", server.actualPort());
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }

    private void initEventBusConsumers() {
        vertx.eventBus().<APIStub>consumer(API_PATH_STUB_CREATE_ADDRESS).handler(mockPathHandler::createPathStubRoute);
        vertx.eventBus().<APIStub>consumer(API_PATH_STUB_UPDATE_ADDRESS).handler(mockPathHandler::updatePathStubRoute);
        vertx.eventBus().<APIStub>consumer(API_PATH_STUB_DELETE_ADDRESS).handler(mockPathHandler::removePathStubRoute);
        vertx.eventBus().<APIService>consumer(API_SERVICE_UPDATE_ADDRESS).handler(mockPathHandler::refreshPathRoutesByServiceId);
        vertx.eventBus().<String>consumer(API_SERVICE_DELETE_ADDRESS).handler(mockPathHandler::removeRoutesByServiceId);

        //runtime
        vertx.eventBus().<APIStub>consumer(API_MOCK_WEBHOOK_ADDRESS).handler(mockPathHandler::fireWebhook);
    }

    private Future<Router> configRouter() {
        final Promise<Router> promise = Promise.promise();
        final Router mockRouter = Router.router(vertx);
        mockRouter.errorHandler(405, new MethodNotAllowedHandler());
        mockRouter.errorHandler(404, new NotFoundErrorHandler());
        RouteBuilder.getInstance(mockRouter.route(API_MOCK_ENDPOINT_PREFIX_WILDCARD)).commonHandler()
            .mountSubRouter(mockSubRouter).build();
        this.loadingExistedRoute()
            .onSuccess(ok -> promise.complete(mockRouter))
            .onFailure(promise::fail);

        return promise.future();
    }

    private Future<Void> loadingExistedRoute() {
        Promise<Void> promise = Promise.promise();
        return apiServiceDao.findAllServiceWithDetails().compose(compositeFuture -> {
            if (compositeFuture.succeeded()) {
                compositeFuture.<APIService>list().forEach(apiService -> vertx.eventBus().publish(API_SERVICE_UPDATE_ADDRESS, apiService));
                log.info("Loading existed route success");
                promise.complete();
            } else {
                log.error("Loading existed route failed", compositeFuture.cause());
                promise.fail(compositeFuture.cause());
            }
            return promise.future();
        }).onFailure(promise::fail);
    }

}
