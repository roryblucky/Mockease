package com.rory.apimock.dao;

import com.rory.apimock.exceptions.ResourceNotFoundException;
import com.rory.apimock.utils.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.ArrayTuple;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.ParamType;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collector;


public abstract class BaseDao<T> {


    protected static final Integer EXISTED = 1;
    protected static final Integer NOT_EXISTED = 0;

    protected final SqlClient sqlClient;

    protected final DSLContext dslContext;

    protected BaseDao(SqlClient sqlClient) {
        this.sqlClient = sqlClient;
        this.dslContext = this.configureDSL();
    }

    private DSLContext configureDSL() {
        Configuration configuration = new DefaultConfiguration();
        configuration.set(SQLDialect.POSTGRES);


        Settings settings = new Settings();
        settings.setRenderFormatted(true);
        settings.setRenderNameCase(RenderNameCase.LOWER);
        settings.setParamType(ParamType.INLINED);

        configuration.set(settings);
        return DSL.using(configuration);
    }

    protected OffsetDateTime currentUTCTime() {
        return DateUtil.getOffsetDateTime();
    }

    protected String formatToString(OffsetDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_INSTANT);
    }


    protected abstract Collector<Row, ?, List<T>> rowCollector();

    protected Future<Void> existed(String sql) {
        return this.execute(sql, (promise, rowSet) -> {
            if (rowSet.size() == EXISTED) {
                promise.complete();
            } else {
                promise.fail(new ResourceNotFoundException());
            }
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

    protected <R> Future<R> executeWithCollectorMapping(String sql, BiConsumer<Promise<R>, SqlResult<List<T>>> consumer) {
        return this.executeWithCollectorMapping(sql, ArrayTuple.EMPTY, consumer);
    }

    protected <R> Future<R> execute(String sql, Tuple params,  BiConsumer<Promise<R>, RowSet<Row>> consumer) {
        Promise<R> promise = Promise.promise();
        this.sqlClient.preparedQuery(sql)
            .execute(params)
            .onSuccess(rowSet -> consumer.accept(promise, rowSet))
            .onFailure(promise::fail);
        return promise.future();
    }

    protected <R> Future<R> execute(String sql, BiConsumer<Promise<R>, RowSet<Row>> consumer) {
        return this.execute(sql, ArrayTuple.EMPTY, consumer);
    }
}
