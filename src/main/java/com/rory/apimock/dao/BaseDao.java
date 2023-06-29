package com.rory.apimock.dao;

import com.rory.apimock.utils.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collector;


public abstract class BaseDao<T> {
    protected final SqlClient sqlClient;

    protected BaseDao(SqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    protected OffsetDateTime currentUTCTime() {
        return DateUtil.getOffsetDateTime();
    }

    protected String formatToString(OffsetDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_INSTANT);
    }


    protected abstract Collector<Row, ?, List<T>> rowCollector();

    protected Future<Boolean> existed(String id, String sql) {
        return this.execute(sql, Tuple.of(id), (promise, rowSet) -> {
            promise.complete(rowSet.iterator().next().getInteger("result") == 1);
        });
    }
    protected <R> Future<R> executeWithCollectorMapping(String sql, Tuple params, BiConsumer<Promise<R>, SqlResult<List<T>>> consumer) {
        Promise<R> promise = Promise.promise();
        this.sqlClient.preparedQuery(sql)
            .collecting(rowCollector())
            .execute(params)
            .onSuccess(sqlResult -> consumer.accept(promise, sqlResult))
            .onFailure(promise::fail);
        return promise.future();
    }

    protected <R> Future<R> execute(String sql, Tuple params,  BiConsumer<Promise<R>, RowSet<Row>> consumer) {
        Promise<R> promise = Promise.promise();
        this.sqlClient.preparedQuery(sql)
            .execute(params)
            .onSuccess(rowSet -> consumer.accept(promise, rowSet))
            .onFailure(promise::fail);
        return promise.future();
    }
}
