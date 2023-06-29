package com.rory.apimock.dao;

import cn.hutool.core.util.IdUtil;
import com.rory.apimock.dto.web.APIService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;

@Slf4j
public class APIServiceDao extends BaseDao {

    public APIServiceDao(SqlClient sqlClient) {
        super(sqlClient);
    }

    @Override
    protected Collector<Row, ?, List> rowCollector() {
        return null;
    }

    public Future<APIService> save(APIService dto) {
        final Promise<APIService> promise = Promise.promise();
        OffsetDateTime now = currentUTCTime();
        dto.setId(IdUtil.fastSimpleUUID());

        final String sql = "insert into API_SERVICE(API_SERVICE_ID, NAME, CATEGORY_ID, DESCRIPTION, PREFIX, VERSION, CREATE_AT, UPDATE_AT) values($1,$2,$3,$4,$5,$6,$7,$8)";
        sqlClient.preparedQuery(sql)
            .execute(Tuple.from(Arrays.asList(dto.getId(),
                dto.getName(),
                "1",
                dto.getDescription(),
                dto.getPrefix(),
                dto.getVersion(),
                now,
                now)))
            .onSuccess(rowSet -> {
                dto.setCreateAt(formatToString(now));
                dto.setUpdateAt(formatToString(now));
                promise.complete(dto);
            }).onFailure(promise::fail);
        return promise.future();
    }
}
