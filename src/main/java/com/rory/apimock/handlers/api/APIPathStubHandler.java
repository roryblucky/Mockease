package com.rory.apimock.handlers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rory.apimock.dao.APIPathStubDao;
import com.rory.apimock.dao.APIServiceDao;
import com.rory.apimock.dto.APIStub;
import com.rory.apimock.dto.web.APIPathDefinition;
import com.rory.apimock.dto.web.APIService;
import com.rory.apimock.dto.web.RequestWrapper;
import com.rory.apimock.dto.web.ResponseWrapper;
import com.rory.apimock.exceptions.DuplicateMockRouteException;
import com.rory.apimock.exceptions.ValidationException;
import com.rory.apimock.utils.BeanValidationUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

import static com.rory.apimock.dto.Constants.API_PATH_STUB_CREATE_ADDRESS;
import static com.rory.apimock.dto.Constants.API_PATH_STUB_DELETE_ADDRESS;

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
                if (validated.getResponse().isProxyEnabled() && validated.getResponse().getProxy() == null) {
                    throw new ValidationException("Proxy is enabled but proxy info is not provided");
                }
                if (validated.getResponse().isWebhookEnabled() && validated.getResponse().getWebhook() == null) {
                    throw new ValidationException("Webhook is enabled but webhook info is not provided");
                }
                return Future.succeededFuture(validated);
            });

        Future.all(this.apiServiceDao.findOne(serviceId), validatedFuture)
            .compose(all -> {
                Future<APIService> apiService = Future.succeededFuture(all.resultAt(0));
                Future<APIPathDefinition> apiPath = this.apiPathStubDao.save(serviceId, request.getData());
                return Future.all(apiService, apiPath);
            })
            .onSuccess(result -> {
                APIStub apiStub = new APIStub(result.<APIService>resultAt(0), result.resultAt(1));
                vertx.eventBus().request(API_PATH_STUB_CREATE_ADDRESS, apiStub, msg -> {
                        if(msg.failed()) {
                            //TODO: Other exception handling?
                            String message = msg.cause().getMessage();
                            ctx.fail(new DuplicateMockRouteException(message));
                        } else {
                            ctx.json(ResponseWrapper.success(ctx, result.resultAt(1)));
                        }

                    });
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
                if (validated.getResponse().isProxyEnabled() && validated.getResponse().getProxy() == null) {
                    throw new ValidationException("Proxy is enabled but proxy info is not provided");
                }
                if (validated.getResponse().isWebhookEnabled() && validated.getResponse().getWebhook() == null) {
                    throw new ValidationException("Webhook is enabled but webhook info is not provided");
                }
                return apiPathStubDao.update(serviceId, pathId, request.getData());
            })
            .onSuccess(updated -> ctx.json(ResponseWrapper.success(ctx, updated)))
            .onFailure(ctx::fail);
    }

    public void deletePathStub(RoutingContext ctx) {
        final String serviceId = ctx.pathParam("serviceId");
        final String pathId = ctx.pathParam("pathId");
        this.apiPathStubDao.findOne(serviceId, pathId)
            .compose(find -> vertx.eventBus().request(API_PATH_STUB_DELETE_ADDRESS, new APIStub(serviceId, find.getOperationId())))
            .compose(ok ->  this.apiPathStubDao.deleteOne(serviceId, pathId))
            .onSuccess(deleted -> ctx.json(ResponseWrapper.noContent(ctx)))
            .onFailure(ctx::fail);
    }
}
