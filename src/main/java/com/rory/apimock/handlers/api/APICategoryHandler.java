package com.rory.apimock.handlers.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rory.apimock.dao.APICategoryDao;
import com.rory.apimock.dto.web.APICategory;
import com.rory.apimock.dto.web.APiCategoryList;
import com.rory.apimock.dto.web.RequestWrapper;
import com.rory.apimock.dto.web.ResponseWrapper;
import com.rory.apimock.utils.BeanValidationUtil;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlClient;

public class APICategoryHandler {

    private final APICategoryDao apiCategoryDao;

    public APICategoryHandler(SqlClient sqlClient) {
        this.apiCategoryDao = new APICategoryDao(sqlClient);
    }

    public void createAPICategory(RoutingContext ctx) {
        RequestWrapper<APICategory> request = JacksonCodec.decodeValue(ctx.body().buffer(), new TypeReference<>() {
        });
        BeanValidationUtil.getInstance().validate(request).onSuccess(valid -> {
            apiCategoryDao.save(valid.getData())
                .onSuccess(saved -> {
                    ctx.json(ResponseWrapper.create(ctx, saved));
                })
                .onFailure(ctx::fail);
        }).onFailure(ctx::fail);
    }

    public void getOneAPICategory(RoutingContext ctx) {
        apiCategoryDao.findOne(ctx.pathParam("categoryId"))
            .onSuccess(result -> ctx.json(ResponseWrapper.ok(ctx, result)))
            .onFailure(ctx::fail);
    }

    public void getAPICategories(RoutingContext ctx) {
        apiCategoryDao.findAll().onSuccess(result -> ctx.json(ResponseWrapper.ok(ctx, new APiCategoryList(result))))
            .onFailure(ctx::fail);
    }

    public void updateAPICategory(RoutingContext ctx) {
        RequestWrapper<APICategory> request = JacksonCodec.decodeValue(ctx.body().buffer(), new TypeReference<>() {
        });
        BeanValidationUtil.getInstance().validate(request).onSuccess(valid -> {
            apiCategoryDao.update(ctx.pathParam("categoryId"), valid.getData())
                .onSuccess(result -> ctx.json(ResponseWrapper.ok(ctx, result)))
                .onFailure(ctx::fail);
        }).onFailure(ctx::fail);
    }

    public void deleteAPICategory(RoutingContext ctx) {
        apiCategoryDao.delete(ctx.pathParam("categoryId")).onSuccess(result -> ctx.json(ResponseWrapper.noContent(ctx)))
            .onFailure(ctx::fail);
    }


}
