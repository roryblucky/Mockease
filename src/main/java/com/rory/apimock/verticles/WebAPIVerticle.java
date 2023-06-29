package com.rory.apimock.verticles;

import com.rory.apimock.handlers.api.APICategoryHandler;
import com.rory.apimock.handlers.api.APIServiceHandler;
import com.rory.apimock.handlers.api.PathStubServiceHandler;
import com.rory.apimock.handlers.error.APIFailureHandler;
import com.rory.apimock.utils.RouteBuilder;
import com.rory.apimock.utils.RouterBuilder;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class WebAPIVerticle extends BaseVerticle {

    private APIServiceHandler apiServiceHandler;
    private PathStubServiceHandler pathStubServiceHandler;

    private APICategoryHandler categoryHandler;


    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.apiServiceHandler = new APIServiceHandler(sqlClient);
        this.categoryHandler = new APICategoryHandler(sqlClient);
        this.pathStubServiceHandler = new PathStubServiceHandler(vertx);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = RouterBuilder.getInstance().router(vertx).build();

        router.route("/static/*").handler(StaticHandler.create("webroot"));

        //session
        SessionStore sessionStore = LocalSessionStore.create(vertx);


        // Management API
        RouteBuilder.getInstance(router.route("/v1/api/*"))
            .handler(SessionHandler.create(sessionStore))
            .commonHandler()
            .mountSubRouter(this.apiRouter());

        this.createAndStartHttpServer(router,
                (int) JsonPointer.from("/http/api/server/port").queryJsonOrDefault(config(), 8080))
            .onSuccess(server -> {
                log.info("Web server start at {}", server.actualPort());
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }

    private Router apiRouter() {
        Router apiRouter = Router.router(vertx);

        apiRouter.route().failureHandler(new APIFailureHandler());

        apiRouter.post("/api-categories").handler(categoryHandler::createAPICategory);
        apiRouter.get("/api-categories").handler(categoryHandler::getAPICategories);
        apiRouter.get("/api-categories/:categoryId").handler(categoryHandler::getOneAPICategory);
        apiRouter.put("/api-categories/:categoryId").handler(categoryHandler::updateAPICategory);
        apiRouter.delete("/api-categories/:categoryId").handler(categoryHandler::deleteAPICategory);


        apiRouter.post("/api-services").handler(apiServiceHandler::createAPIService);
        apiRouter.get("/api-services").handler(apiServiceHandler::getAPIServices);
        apiRouter.put("/api-services/:serviceId").handler(apiServiceHandler::updateAPIService);
        apiRouter.delete("/api-services/:serviceId").handler(apiServiceHandler::deleteAPIServices);

        apiRouter.post("/api-services/:serviceId/paths").handler(pathStubServiceHandler::createPathStub);
        apiRouter.get("/api-services/:serviceId/paths").handler(pathStubServiceHandler::getPaths);
        apiRouter.put("/api-services/:serviceId/paths/:pathId").handler(pathStubServiceHandler::updatePathStub);
        apiRouter.delete("/api-services/:serviceId/paths/:pathId").handler(pathStubServiceHandler::deletePathStub);

        return apiRouter;
    }


}
