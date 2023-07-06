package com.rory.apimock.dao;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.rory.apimock.db.tables.records.ApiPathStubRecord;
import com.rory.apimock.dto.web.APIPathDefinition;
import com.rory.apimock.dto.web.RequestInfo;
import com.rory.apimock.dto.web.ResponseInfo;
import com.rory.apimock.exceptions.ResourceNotFoundException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import org.jooq.InsertSetMoreStep;
import org.jooq.UpdateSetMoreStep;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.rory.apimock.db.Tables.API_PATH_STUB;

public class APIPathStubDao extends BaseDao<APIPathDefinition> {

    public APIPathStubDao(SqlClient sqlClient) {
        super(sqlClient);
    }

    public Future<APIPathDefinition> save(String serviceId, APIPathDefinition dto) {
        dto.setId(IdUtil.fastSimpleUUID());
        OffsetDateTime now = currentUTCTime();
        InsertSetMoreStep<ApiPathStubRecord> insertSQL = dslContext.insertInto(API_PATH_STUB)
            .set(API_PATH_STUB.API_PATH_STUB_ID, dto.getId())
            .set(API_PATH_STUB.API_SERVICE_ID, serviceId)
            .set(API_PATH_STUB.NAME, dto.getName().toLowerCase())
            .set(API_PATH_STUB.OPERATION_ID, dto.getOperationId())
            .set(API_PATH_STUB.DESCRIPTION, dto.getDescription())
            .set(API_PATH_STUB.PATH, dto.getRequest().getPath())
            .set(API_PATH_STUB.METHOD, dto.getRequest().getMethod())
            .set(API_PATH_STUB.REQUEST_HEADERS, new JsonObject(dto.getRequest().getHeaders()).encode())
            .set(API_PATH_STUB.VALIDATION_ENABLED, dto.getRequest().isValidationEnabled())
            .set(API_PATH_STUB.REQUEST_SCHEMA, dto.getRequest().getSchema())
            .set(API_PATH_STUB.REQUEST_DYNAMIC_BODY, dto.getRequest().isDynamicBodyEnabled())
            .set(API_PATH_STUB.REQUEST_BODY, dto.getRequest().getBody())
            .set(API_PATH_STUB.RESPONSE_HTTP_STATUS, dto.getResponse().getHttpStatus())
            .set(API_PATH_STUB.RESPONSE_HEADERS, new JsonObject(dto.getResponse().getHeaders()).encode())
            .set(API_PATH_STUB.RESPONSE_DYNAMIC_BODY, dto.getResponse().isDynamicBodyEnabled())
            .set(API_PATH_STUB.RESPONSE_BODY, dto.getResponse().getBody())
            .set(API_PATH_STUB.RESPONSE_WEBHOOK_ENABLED, dto.getResponse().isWebhookEnabled())
            .set(API_PATH_STUB.RESPONSE_PROXY_ENABLED, dto.getResponse().isProxyEnabled())
            .set(API_PATH_STUB.UPDATE_AT, now)
            .set(API_PATH_STUB.CREATE_AT, now);

        if (dto.getResponse().isWebhookEnabled()) {
            insertSQL
                .set(API_PATH_STUB.RESPONSE_WEBHOOK_DYNAMIC_BODY, dto.getResponse().getWebhook().isDynamicBodyEnabled())
                .set(API_PATH_STUB.RESPONSE_WEBHOOK_URL, dto.getResponse().getWebhook().getUrl())
                .set(API_PATH_STUB.RESPONSE_WEBHOOK_METHOD, dto.getResponse().getWebhook().getMethod())
                .set(API_PATH_STUB.RESPONSE_WEBHOOK_HEADERS, new JsonObject(dto.getResponse().getWebhook().getHeaders()).encode())
                .set(API_PATH_STUB.RESPONSE_WEBHOOK_BODY, dto.getResponse().getWebhook().getBody());
        }

        if (dto.getResponse().isProxyEnabled()) {
            insertSQL.set(API_PATH_STUB.RESPONSE_PROXY_URL, dto.getResponse().getProxy().getUrl());
        }

        final String sql = insertSQL.getSQL();
        return this.execute(sql, ((promise, rowSet) -> {
            dto.setUpdateAt(formatToString(now));
            dto.setCreateAt(formatToString(now));
            promise.complete(dto);
        }));
    }

    public Future<List<APIPathDefinition>> findByServiceId(String serviceId) {
        final String sql = dslContext.selectFrom(API_PATH_STUB)
            .where(API_PATH_STUB.API_SERVICE_ID.eq(serviceId))
            .getSQL();
        return this.executeWithCollectorMapping(sql, (promise, listSqlResult) ->
           promise.complete(listSqlResult.value())
        );
    }

    public Future<APIPathDefinition> findOne(String serviceId, String id) {
        final String sql = dslContext.selectFrom(API_PATH_STUB)
            .where(API_PATH_STUB.API_PATH_STUB_ID.eq(id))
            .and(API_PATH_STUB.API_SERVICE_ID.eq(serviceId))
            .getSQL();
        return this.executeWithCollectorMapping(sql, (promise, sqlResult) ->
           sqlResult.value().stream().findAny()
               .ifPresentOrElse(promise::complete, () -> promise.fail(new ResourceNotFoundException()))
        );
    }

    public Future<Void> deleteOne(String serviceId, String id) {
        final String sql = dslContext.deleteFrom(API_PATH_STUB)
            .where(API_PATH_STUB.API_PATH_STUB_ID.eq(id))
            .and(API_PATH_STUB.API_SERVICE_ID.eq(serviceId))
            .getSQL();
        return this.execute(sql, (promise, rowSet) -> promise.complete());
    }

    public Future<Void> deleteByServiceId(String serviceId) {
        final String sql = dslContext.deleteFrom(API_PATH_STUB)
            .where(API_PATH_STUB.API_SERVICE_ID.eq(serviceId))
            .getSQL();
        return this.execute(sql, (promise, rowSet) -> promise.complete());
    }

    public Future<APIPathDefinition> update(String serviceId, String pathId, APIPathDefinition dto) {
        OffsetDateTime now = currentUTCTime();

        UpdateSetMoreStep<ApiPathStubRecord> updateStep = dslContext.update(API_PATH_STUB)
            .set(API_PATH_STUB.NAME, dto.getName())
            .set(API_PATH_STUB.OPERATION_ID, dto.getOperationId())
            .set(API_PATH_STUB.UPDATE_AT, now)
            .set(API_PATH_STUB.PATH, dto.getRequest().getPath())
            .set(API_PATH_STUB.METHOD, dto.getRequest().getMethod())
            .set(API_PATH_STUB.REQUEST_HEADERS, new JsonObject(dto.getRequest().getHeaders()).encode())
            .set(API_PATH_STUB.VALIDATION_ENABLED, dto.getRequest().isValidationEnabled())
            .set(API_PATH_STUB.REQUEST_DYNAMIC_BODY, dto.getRequest().isDynamicBodyEnabled())
            .set(API_PATH_STUB.RESPONSE_HTTP_STATUS, dto.getResponse().getHttpStatus())
            .set(API_PATH_STUB.RESPONSE_HEADERS, new JsonObject(dto.getResponse().getHeaders()).encode())
            .set(API_PATH_STUB.RESPONSE_DYNAMIC_BODY, dto.getResponse().isDynamicBodyEnabled())
            .set(API_PATH_STUB.RESPONSE_BODY, dto.getRequest().getBody())
            .set(API_PATH_STUB.RESPONSE_WEBHOOK_ENABLED, dto.getResponse().isWebhookEnabled())
            .set(API_PATH_STUB.RESPONSE_PROXY_ENABLED, dto.getResponse().isProxyEnabled());

        if (StrUtil.isNotEmpty(dto.getDescription())) {
            updateStep.set(API_PATH_STUB.DESCRIPTION, dto.getDescription());
        }
        //request optional fields
        if (StrUtil.isNotEmpty(dto.getRequest().getSchema())) {
            updateStep.set(API_PATH_STUB.REQUEST_SCHEMA, dto.getRequest().getSchema());
        }
        if (StrUtil.isNotEmpty(dto.getRequest().getBody())) {
            updateStep.set(API_PATH_STUB.REQUEST_BODY, dto.getRequest().getBody());
        }
        //response optional fields
        if (dto.getResponse().isWebhookEnabled()) {
            updateStep.set(API_PATH_STUB.RESPONSE_WEBHOOK_URL, dto.getResponse().getWebhook().getUrl())
                .set(API_PATH_STUB.RESPONSE_WEBHOOK_METHOD, dto.getResponse().getWebhook().getMethod())
                .set(API_PATH_STUB.RESPONSE_WEBHOOK_HEADERS, new JsonObject(dto.getResponse().getWebhook().getHeaders()).encode())
                .set(API_PATH_STUB.RESPONSE_WEBHOOK_BODY, dto.getResponse().getWebhook().getBody());
        }

        if (dto.getResponse().isProxyEnabled()) {
            updateStep.set(API_PATH_STUB.RESPONSE_PROXY_URL, dto.getResponse().getProxy().getUrl());
        }

        final String updateSQL = updateStep.where(API_PATH_STUB.API_PATH_STUB_ID.eq(pathId))
            .and(API_PATH_STUB.API_SERVICE_ID.eq(serviceId)).getSQL();

        return this.execute(updateSQL, ((promise, rowSet) ->
            this.findOne(serviceId, pathId).onSuccess(promise::complete).onFailure(promise::fail)));
    }

    @Override
    protected Collector<Row, ?, List<APIPathDefinition>> rowMappingCollector() {
        APIPathDefinition apiPathDefinition = new APIPathDefinition();
        //request
        RequestInfo requestInfo = new RequestInfo();
        apiPathDefinition.setRequest(requestInfo);
        //Response
        ResponseInfo responseInfo = new ResponseInfo();
        apiPathDefinition.setResponse(responseInfo);

        return Collectors.mapping(
            row -> {
                apiPathDefinition.setId(row.getString(API_PATH_STUB.API_PATH_STUB_ID.getName().toLowerCase()));
                apiPathDefinition.setName(row.getString(API_PATH_STUB.NAME.getName().toLowerCase()));
                apiPathDefinition.setOperationId(row.getString(API_PATH_STUB.OPERATION_ID.getName().toLowerCase()));
                apiPathDefinition.setDescription(row.getString(API_PATH_STUB.DESCRIPTION.getName().toLowerCase()));
                apiPathDefinition.setUpdateAt(formatToString(row.getOffsetDateTime(API_PATH_STUB.UPDATE_AT.getName().toLowerCase())));
                apiPathDefinition.setCreateAt(formatToString(row.getOffsetDateTime(API_PATH_STUB.CREATE_AT.getName().toLowerCase())));
                //request
                requestInfo.setPath(row.getString(API_PATH_STUB.PATH.getName().toLowerCase()));
                requestInfo.setMethod(row.getString(API_PATH_STUB.METHOD.getName().toLowerCase()));
                requestInfo.setHeaders(new JsonObject(row.getString(API_PATH_STUB.REQUEST_HEADERS.getName().toLowerCase())).getMap());
                requestInfo.setValidationEnabled(row.getBoolean(API_PATH_STUB.VALIDATION_ENABLED.getName().toLowerCase()));
                requestInfo.setSchema(row.getString(API_PATH_STUB.REQUEST_SCHEMA.getName().toLowerCase()));
                requestInfo.setDynamicBodyEnabled(row.getBoolean(API_PATH_STUB.REQUEST_DYNAMIC_BODY.getName().toLowerCase()));
                requestInfo.setBody(row.getString(API_PATH_STUB.REQUEST_BODY.getName().toLowerCase()));
                //response
                responseInfo.setHttpStatus(row.getInteger(API_PATH_STUB.RESPONSE_HTTP_STATUS.getName().toLowerCase()));
                responseInfo.setHeaders(new JsonObject(row.getString(API_PATH_STUB.RESPONSE_HEADERS.getName().toLowerCase())).getMap());
                responseInfo.setDynamicBodyEnabled(row.getBoolean(API_PATH_STUB.RESPONSE_DYNAMIC_BODY.getName().toLowerCase()));
                responseInfo.setBody(row.getString(API_PATH_STUB.RESPONSE_BODY.getName().toLowerCase()));
                responseInfo.setWebhookEnabled(row.getBoolean(API_PATH_STUB.RESPONSE_WEBHOOK_ENABLED.getName().toLowerCase()));
                responseInfo.setProxyEnabled(row.getBoolean(API_PATH_STUB.RESPONSE_PROXY_ENABLED.getName().toLowerCase()));
                //webhook
                if (responseInfo.isWebhookEnabled()) {
                    ResponseInfo.WebhookInfo webhookInfo = new ResponseInfo.WebhookInfo();
                    responseInfo.setWebhook(webhookInfo);
                    webhookInfo.setUrl(row.getString(API_PATH_STUB.RESPONSE_WEBHOOK_URL.getName().toLowerCase()));
                    webhookInfo.setMethod(row.getString(API_PATH_STUB.RESPONSE_WEBHOOK_METHOD.getName().toLowerCase()));
                    webhookInfo.setHeaders(new JsonObject(row.getString(API_PATH_STUB.RESPONSE_WEBHOOK_HEADERS.getName().toLowerCase())).getMap());
                    webhookInfo.setDynamicBodyEnabled(row.getBoolean(API_PATH_STUB.RESPONSE_WEBHOOK_DYNAMIC_BODY.getName().toLowerCase()));
                    webhookInfo.setBody(row.getString(API_PATH_STUB.RESPONSE_WEBHOOK_BODY.getName().toLowerCase()));
                }
                //proxy
                if (responseInfo.isProxyEnabled()) {
                    ResponseInfo.Proxy proxy = new ResponseInfo.Proxy();
                    responseInfo.setProxy(proxy);
                    proxy.setUrl(row.getString(API_PATH_STUB.RESPONSE_PROXY_URL.getName().toLowerCase()));
                }
                return apiPathDefinition;
            },
            Collectors.toList()
        );
    }
}
