package com.rory.apimock.dao;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.rory.apimock.db.tables.records.ApiWebhookRecord;
import com.rory.apimock.dto.web.ResponseInfo;
import com.rory.apimock.exceptions.ResourceNotFoundException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import org.jooq.UpdateSetMoreStep;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.rory.apimock.db.Tables.API_WEBHOOK;

public class APIWebhookDao extends BaseDao<ResponseInfo.WebhookInfo> {

    protected APIWebhookDao(SqlClient sqlClient) {
        super(sqlClient);
    }

    public Future<ResponseInfo.WebhookInfo> save(ResponseInfo.WebhookInfo dto) {
        OffsetDateTime now = currentUTCTime();
        dto.setId(IdUtil.fastSimpleUUID());
        final String sql = dslContext.insertInto(API_WEBHOOK, API_WEBHOOK.API_WEBHOOK_ID, API_WEBHOOK.URL,
                API_WEBHOOK.METHOD, API_WEBHOOK.HEADERS, API_WEBHOOK.REQUEST_DYNAMIC_BODY, API_WEBHOOK.BODY,
                API_WEBHOOK.CREATE_AT, API_WEBHOOK.UPDATE_AT)
            .values(dto.getId(), dto.getUrl(), dto.getMethod(), new JsonObject(dto.getHeaders()).encode(),
                dto.isDynamicBody(), dto.getBody(), now, now).getSQL();
        return this.execute(sql, (promise, rowSet) -> promise.complete(dto));
    }

    public Future<Void> delete(String id) {
        final String sql = dslContext.delete(API_WEBHOOK).where(API_WEBHOOK.API_WEBHOOK_ID.eq(id)).getSQL();
        return this.execute(sql, (promise, rowSet) -> promise.complete());
    }

    public Future<ResponseInfo.WebhookInfo> update(String id, ResponseInfo.WebhookInfo dto) {
        UpdateSetMoreStep<ApiWebhookRecord> updateRecord = dslContext.update(API_WEBHOOK).set(API_WEBHOOK.URL, dto.getUrl())
            .set(API_WEBHOOK.METHOD, dto.getMethod())
            .set(API_WEBHOOK.BODY, dto.getBody())
            .set(API_WEBHOOK.REQUEST_DYNAMIC_BODY, dto.isDynamicBody())
            .set(API_WEBHOOK.UPDATE_AT, currentUTCTime());
        if (dto.getHeaders() != null && CollectionUtil.isNotEmpty(dto.getHeaders())) {
            updateRecord.set(API_WEBHOOK.HEADERS, new JsonObject(dto.getHeaders()).encode());
        }
        final String finalSql = updateRecord.getSQL();
        return this.execute(finalSql, (proxyPromise, rowSet) -> proxyPromise.complete(dto));
    }

    public Future<ResponseInfo.WebhookInfo> findOne(String id) {
        final String sql = dslContext.select(API_WEBHOOK.API_WEBHOOK_ID, API_WEBHOOK.URL,
            API_WEBHOOK.METHOD, API_WEBHOOK.HEADERS, API_WEBHOOK.REQUEST_DYNAMIC_BODY, API_WEBHOOK.BODY,
            API_WEBHOOK.CREATE_AT, API_WEBHOOK.UPDATE_AT).where(API_WEBHOOK.API_WEBHOOK_ID.eq(id)).getSQL();
        return this.executeWithCollectorMapping(sql, (proxyPromise, listSqlResult) -> {
            Optional<ResponseInfo.WebhookInfo> any = listSqlResult.value().stream().findAny();
            if (any.isPresent()) {
                proxyPromise.complete(any.get());
            } else {
                proxyPromise.fail(new ResourceNotFoundException());
            }
        });
    }



    @Override
    protected Collector<Row, ?, List<ResponseInfo.WebhookInfo>> rowMappingCollector() {
        return Collectors.mapping(
            row -> new ResponseInfo.WebhookInfo(row.getString("api_webhook_id"),
                row.getString("url"),
                row.getString("method"),
                row.getBoolean("request_dynamic_body"),
                JsonObject.mapFrom(row.getString("headers")).getMap(),
                row.getString("body")),
            Collectors.toList()
        );
    }
}
