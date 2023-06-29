package com.rory.apimock.dao;

import com.rory.apimock.dto.web.APICategory;
import com.rory.apimock.utils.DateUtil;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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


}
