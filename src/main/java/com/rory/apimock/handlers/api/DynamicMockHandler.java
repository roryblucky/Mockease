package com.rory.apimock.handlers.api;

import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.Router;

public class DynamicMockHandler {

    private final Router mockRouter;

    public DynamicMockHandler(Router router) {
        this.mockRouter = router;
    }

    public void createMockService(Message<Object> msg) {
//        final PathDefinition pathDefinition = (PathDefinition) msg.body();
//        pathDefinition.getPaths().forEach(endpoint -> {
//            mockRouter.route(HttpMethod.valueOf(endpoint.getMethod()), String.format("/%s%s%s", pathDefinition.getVersion(), pathDefinition.getPrefix(), endpoint.getPath()))
//                .produces(endpoint.getContentType())
//                .handler(ctx -> {
//                    ctx.json(endpoint.getResponseSchema());
//                });
//        });
//        msg.reply("create finished");
    }
}
