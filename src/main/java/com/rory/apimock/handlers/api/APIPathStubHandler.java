package com.rory.apimock.handlers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rory.apimock.dao.APIPathStubDao;
import com.rory.apimock.dao.APIServiceDao;
import com.rory.apimock.dto.web.APIPathDefinition;
import com.rory.apimock.dto.web.RequestWrapper;
import com.rory.apimock.dto.web.ResponseWrapper;
import com.rory.apimock.utils.BeanValidationUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

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
        this.apiServiceDao.checkExisted(serviceId)
            .compose(found -> BeanValidationUtil.getInstance().validate(request.getData()))
            .compose(validated -> this.apiPathStubDao.save(serviceId, request.getData()))
            .onSuccess(saved -> ctx.json(ResponseWrapper.create(ctx, saved)))
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
    }

    public void deletePathStub(RoutingContext ctx) {
        final String serviceId = ctx.pathParam("serviceId");
        final String pathId = ctx.pathParam("pathId");
        this.apiPathStubDao.deleteOne(serviceId, pathId)
            .onSuccess(deleted -> ctx.json(ResponseWrapper.noContent(ctx)))
            .onFailure(ctx::fail);
    }
}
