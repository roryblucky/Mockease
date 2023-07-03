package com.rory.apimock.handlers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rory.apimock.dao.APIServiceDao;
import com.rory.apimock.dto.web.APIService;
import com.rory.apimock.dto.web.APIServiceList;
import com.rory.apimock.dto.web.RequestWrapper;
import com.rory.apimock.dto.web.ResponseWrapper;
import com.rory.apimock.utils.BeanValidationUtil;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class APIServiceHandler {


    private final APIServiceDao apiServiceDao;

    public APIServiceHandler(SqlClient sqlClient) {
        this.apiServiceDao = new APIServiceDao(sqlClient);
    }

    public void createAPIService(RoutingContext ctx) {
        RequestWrapper<APIService> request = JacksonCodec.decodeValue(ctx.body().buffer(), new TypeReference<>() {
        });
        BeanValidationUtil.getInstance().validate(request)
            .compose(req -> apiServiceDao.save(req.getData()))
            .onSuccess(saved -> ctx.json(ResponseWrapper.create(ctx, saved)))
            .onFailure(ctx::fail);
    }


    public void updateAPIService(RoutingContext ctx) {
        RequestWrapper<APIService> request = JacksonCodec.decodeValue(ctx.body().buffer(), new TypeReference<>() {
        });
        BeanValidationUtil.getInstance().validate(request)
            .compose(req -> apiServiceDao.update(ctx.pathParam("serviceId"), req.getData()))
            .onSuccess(saved -> ctx.json(ResponseWrapper.success(ctx, saved)))
            .onFailure(ctx::fail);
    }

    public void getAPIServices(RoutingContext ctx) {
        apiServiceDao.findAll()
            .onSuccess(results -> ctx.json(ResponseWrapper.success(ctx, new APIServiceList(results))))
            .onFailure(ctx::fail);
    }

    public void getOneAPIService(RoutingContext ctx) {
        String serviceId = ctx.pathParam("serviceId");
        apiServiceDao.findOne(serviceId)
            .onSuccess(results -> ctx.json(ResponseWrapper.success(ctx, results)))
            .onFailure(ctx::fail);
    }

    public void deleteAPIServices(RoutingContext ctx) {
        String serviceId = ctx.pathParam("serviceId");
        apiServiceDao.delete(serviceId)
            .onSuccess(results -> ctx.json(ResponseWrapper.noContent(ctx)))
            .onFailure(ctx::fail);
    }
}
