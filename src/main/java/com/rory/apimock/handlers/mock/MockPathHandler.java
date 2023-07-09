package com.rory.apimock.handlers.mock;

import com.github.jknack.handlebars.Template;
import com.rory.apimock.dto.APIStub;
import com.rory.apimock.dto.web.APIService;
import com.rory.apimock.handlers.mock.runtime.MockRouterHelper;
import com.rory.apimock.utils.TemplateCacheUtil;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockPathHandler {

    private final MockRouterHelper mockRouterHelper;

    private final TemplateCacheUtil<Template> templateCacheUtil;

    public MockPathHandler(Vertx vertx, Router router) {
        this.mockRouterHelper = new MockRouterHelper(vertx, router);
        templateCacheUtil = new TemplateCacheUtil<>(vertx);
    }

    public void createPathStubRoute(Message<APIStub> message) {
        APIStub apiStub = message.body();
        log.info("Creating route: {}", apiStub.getIdentifier());
        mockRouterHelper.createRoute(apiStub);
    }

    public void removePathStubRoute(Message<APIStub> message) {
        APIStub apiStub = message.body();
        log.info("Removing route: {}", apiStub.getIdentifier());
        mockRouterHelper.removeRoute(apiStub);
        templateCacheUtil.removeKeyIfPresent(apiStub.getOperationId());
    }

    public void updatePathStubRoute(Message<APIStub> message) {
        APIStub apiStub = message.body();
        log.info("Updating route: {}", apiStub.getIdentifier());
        mockRouterHelper.removeRoute(apiStub);
        mockRouterHelper.createRoute(apiStub);
        templateCacheUtil.removeKeyIfPresent(apiStub.getOperationId());
    }

    public void refreshPathRoutesByServiceId(Message<APIService> message) {
        APIService apiService = message.body();
        log.info("Refreshing routes for service: {} - {}", apiService.getId(),apiService.getName());
        mockRouterHelper.removeAllRoutesOnService(apiService.getId());
        templateCacheUtil.clearCache();
        // recreate routes
        apiService.getPathStubs().forEach(pathDefinition ->
            mockRouterHelper.createRoute(new APIStub(apiService, pathDefinition))
        );
    }

    public void removeRoutesByServiceId(Message<String> message) {
        log.info("Removing routes for service: {}", message.body());
        //TODO: clear cache
        mockRouterHelper.removeAllRoutesOnService(message.body());
    }


    public void fireWebhook(Message<APIStub> message) {
        log.info("Firing webhook for: {}", message.body().getIdentifier());
        mockRouterHelper.fireWebhook(message.body());
    }
}
