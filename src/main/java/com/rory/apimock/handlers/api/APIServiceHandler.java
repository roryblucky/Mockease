package com.rory.apimock.handlers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rory.apimock.dao.APIServiceDao;
import com.rory.apimock.dto.web.APIService;
import com.rory.apimock.dto.web.RequestWrapper;
import com.rory.apimock.dto.web.ResponseWrapper;
import com.rory.apimock.utils.BeanValidationUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
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
            .onSuccess(saved -> {
                ctx.json(ResponseWrapper.create(ctx, saved));
            })
            .onFailure(ctx::fail);
    }


    public void updateAPIService(RoutingContext ctx) {

    }

    public void getAPIServices(RoutingContext ctx) {
        ctx.json("ok");
    }

    public void deleteAPIServices(RoutingContext ctx) {
        ctx.json("ok");
    }
}
