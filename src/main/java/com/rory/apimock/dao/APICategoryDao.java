package com.rory.apimock.dao;

import cn.hutool.core.util.IdUtil;
import com.rory.apimock.dto.web.APICategory;
import com.rory.apimock.exceptions.ResourceNotFoundException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class APICategoryDao extends BaseDao<APICategory> {
    public APICategoryDao(SqlClient sqlClient) {
        super(sqlClient);
    }

    public Future<APICategory> save(APICategory dto) {
        Promise<APICategory> promise = Promise.promise();
        OffsetDateTime now = currentUTCTime();
        dto.setId(IdUtil.fastSimpleUUID());

        final String sql = "INSERT INTO API_CATEGORY(API_CATEGORY_ID, NAME, DESCRIPTION, CREATE_AT, UPDATE_AT) VALUES($1, $2, $3, $4, $5)";

        this.sqlClient.preparedQuery(sql)
            .execute(Tuple.from(Arrays.asList(dto.getId(),
                dto.getName(),
                dto.getDescription(),
                now,
                now)))
            .onSuccess(rowSet -> {
                dto.setCreateAt(formatToString(now));
                dto.setUpdateAt(formatToString(now));
                promise.complete(dto);
            })
            .onFailure(promise::fail);
        return promise.future();
    }

    public Future<APICategory> update(String id, APICategory dto) {
        Promise<APICategory> promise = Promise.promise();

        return this.findOne(id).compose(found -> {
            OffsetDateTime now = currentUTCTime();

            final String sql = "UPDATE API_CATEGORY SET NAME = $1, DESCRIPTION = $2, UPDATE_AT = $3 WHERE API_CATEGORY_ID = $4";

            this.sqlClient.preparedQuery(sql)
                .execute(Tuple.from(Arrays.asList(
                    dto.getName(),
                    dto.getDescription(),
                    now,
                    id)))
                .onSuccess(rowSet -> {
                    dto.setUpdateAt(formatToString(now));
                    promise.complete(dto);
                })
                .onFailure(promise::fail);
            return promise.future();
        });
    }

    public Future<Void> delete(String id) {
        Promise<Void> promise = Promise.promise();
        return this.findOne(id).compose(found -> {
            final String sql = "DELETE FROM API_CATEGORY WHERE API_CATEGORY_ID = $1";

            this.sqlClient.preparedQuery(sql)
                .execute(Tuple.of(id))
                .onSuccess(rowSet -> promise.complete())
                .onFailure(promise::fail);
            return promise.future();
        });
    }

    public Future<APICategory> findOne(String id) {
        Promise<APICategory> promise = Promise.promise();
        final String sql = "SELECT API_CATEGORY_ID, NAME, DESCRIPTION, CREATE_AT, UPDATE_AT FROM API_CATEGORY WHERE API_CATEGORY_ID = $1";

        this.sqlClient.preparedQuery(sql)
            .collecting(rowCollector())
            .execute(Tuple.of(id))
            .onSuccess(sqlResult -> {
                Optional<APICategory> any = sqlResult.value().stream().findAny();
                if (any.isPresent()) {
                    promise.complete(any.get());
                } else {
                    promise.fail(new ResourceNotFoundException());
                }
            })
            .onFailure(promise::fail);
        return promise.future();
    }



    public Future<List<APICategory>> findAll() {
        Promise<List<APICategory>> promise = Promise.promise();
        final String sql = "SELECT API_CATEGORY_ID, NAME, DESCRIPTION, CREATE_AT, UPDATE_AT FROM API_CATEGORY";

        this.sqlClient.preparedQuery(sql)
            .collecting(rowCollector())
            .execute()
            .onSuccess(sqlResult -> {
                promise.complete(sqlResult.value());
            })
            .onFailure(promise::fail);
        return promise.future();
    }

    protected Collector<Row, ?, List<APICategory>> rowCollector() {
         return Collectors.mapping(
            row -> new APICategory(row.getString("api_category_id"),
                formatToString(row.getOffsetDateTime("create_at")),
                formatToString(row.getOffsetDateTime("update_at")),
                row.getString("name"),
                row.getString("description")),
            Collectors.toList()
        );
    }
}
