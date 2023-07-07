package com.rory.apimock.handlers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rory.apimock.dao.APIPathStubDao;
import com.rory.apimock.dao.APIServiceDao;
import com.rory.apimock.dto.APIStub;
import com.rory.apimock.dto.web.APIPathDefinition;
import com.rory.apimock.dto.web.APIService;
import com.rory.apimock.dto.web.RequestWrapper;
import com.rory.apimock.dto.web.ResponseWrapper;
import com.rory.apimock.exceptions.ValidationException;
import com.rory.apimock.utils.BeanValidationUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

import static com.rory.apimock.dto.Constants.*;

@Slf4j
public class APIPathStubHandler {

    private final Vertx vertx;

    private final APIPathStubDao apiPathStubDao;

    private final APIServiceDao apiServiceDao;

    public APIPathStubHandler(Vertx vertx, SqlClient sqlClient) {
        this.vertx = vertx;
        this.apiPathStubDao = new APIPathStubDao(sqlClient);
        this.apiServiceDao = new APIServiceDao(sqlClient);
    }

    public void createPathStub(RoutingContext ctx) {
        RequestWrapper<APIPathDefinition> request = JacksonCodec.decodeValue(ctx.body().buffer(), new TypeReference<>() {
        });
        String serviceId = ctx.pathParam("serviceId");
        Future<APIPathDefinition> validatedFuture = BeanValidationUtil.getInstance().validate(request.getData())
            .compose(validated -> {
                logicValidation(validated);
                return Future.succeededFuture(validated);
            });

        Future.all(this.apiServiceDao.findOne(serviceId), validatedFuture)
            .compose(all -> {
                Future<APIService> apiService = Future.succeededFuture(all.resultAt(0));
                Future<APIPathDefinition> apiPath = this.apiPathStubDao.checkUnique(serviceId, request.getData())
                    .compose(newPath -> this.apiPathStubDao.save(serviceId, newPath));
                return Future.all(apiService, apiPath);
            })
            .onSuccess(result -> {
                ctx.json(ResponseWrapper.success(ctx, result.resultAt(1)));
                vertx.eventBus().publish(API_PATH_STUB_CREATE_ADDRESS, new APIStub(result.<APIService>resultAt(0), result.resultAt(1)));
            })
            .onFailure(ctx::fail);
    }

    public void getPaths(RoutingContext ctx) {
        final String serviceId = ctx.pathParam("serviceId");
        final String pathId = ctx.pathParam("pathId");
        this.apiPathStubDao.findOne(serviceId, pathId)
            .onSuccess(paths -> ctx.json(ResponseWrapper.success(ctx, paths)))
            .onFailure(ctx::fail);
    }

    public void updatePathStub(RoutingContext ctx) {
        final String serviceId = ctx.pathParam("serviceId");
        final String pathId = ctx.pathParam("pathId");
        RequestWrapper<APIPathDefinition> request = JacksonCodec.decodeValue(ctx.body().buffer(), new TypeReference<>() {
        });
        BeanValidationUtil.getInstance().validate(request.getData())
            .compose(validated -> {
                logicValidation(validated);
                return apiPathStubDao.update(serviceId, pathId, request.getData());
            })
            .compose(updated -> Future.all(this.apiServiceDao.findOne(serviceId), Future.succeededFuture(updated)))
            .onSuccess(updated -> {
                ctx.json(ResponseWrapper.success(ctx, updated.resultAt(1)));
                vertx.eventBus().publish(API_PATH_STUB_UPDATE_ADDRESS, new APIStub( updated.<APIService>resultAt(0), updated.resultAt(1)));
            })
            .onFailure(ctx::fail);
    }

    private void logicValidation(APIPathDefinition validated) {
               if (validated.getResponse().isProxyEnabled() && validated.getResponse().getProxy() == null) {
            throw new ValidationException("Proxy is enabled but proxy info is not provided");
        }
        if (validated.getResponse().isWebhookEnabled() && validated.getResponse().getWebhook() == null) {
            throw new ValidationException("Webhook is enabled but webhook info is not provided");
        }
    }

    public void deletePathStub(RoutingContext ctx) {
        final String serviceId = ctx.pathParam("serviceId");
        final String pathId = ctx.pathParam("pathId");
        this.apiPathStubDao.findOne(serviceId, pathId)
            .compose(find -> {
                vertx.eventBus().publish(API_PATH_STUB_DELETE_ADDRESS, new APIStub(serviceId, find.getOperationId()));
                return Future.succeededFuture(find);
            })
            .compose(ok -> this.apiPathStubDao.deleteOne(serviceId, pathId))
            .onSuccess(deleted -> ctx.json(ResponseWrapper.noContent(ctx)))
            .onFailure(ctx::fail);
    }
}
