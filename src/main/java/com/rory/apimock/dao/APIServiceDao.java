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
public class APIServiceDao extends BaseDao<APIService> {

    private final APICategoryDao categoryDao;

    public APIServiceDao(SqlClient sqlClient) {
        super(sqlClient);
        this.categoryDao = new APICategoryDao(sqlClient);
    }

    @Override
    protected Collector<Row, ?, List<APIService>> rowCollector() {
        return null;
    }

    public Future<APIService> save(APIService dto) {
        return categoryDao.findOne(dto.getCategoryId()).compose(existed -> {
            final String sql = "INSERT INTO API_SERVICE(API_SERVICE_ID, NAME, CATEGORY_ID, DESCRIPTION, PREFIX, VERSION, CREATE_AT, UPDATE_AT) VALUES($1,$2,$3,$4,$5,$6,$7,$8)";
            dto.setId(IdUtil.fastSimpleUUID());
            dto.setCategoryId(existed.getId());
            dto.setCategoryName(existed.getName());
            OffsetDateTime now = currentUTCTime();

            return this.execute(sql,
                Tuple.from(Arrays.asList(dto.getId(), dto.getName(), dto.getCategoryId(), dto.getDescription(), dto.getPrefix(),
                    dto.getVersion(), now, now)),
                (promise, rowSet) -> {
                    dto.setCreateAt(formatToString(now));
                    dto.setUpdateAt(formatToString(now));
                    promise.complete(dto);
                });
        });
    }

    public Future<APIService> update(String id, APIService dto) {
//        return this.checkExisted(id).compose(found -> {
//            final OffsetDateTime now = currentUTCTime();
//            final String sql = "UPDATE API_SERVICE SET NAME = $1, DESCRIPTION = $2, PREFIX = $3, VERSION = $4, , CATEGORY_ID = $5, UPDATE_AT = $6 WHERE API_SERVICE_ID = $7";
//            return this.execute(sql,
//                Tuple.from(Arrays.asList(dto.getName(), dto.getDescription(), dto.getPrefix(), dto.getDescription(), now, id)),
//                (promise, rowSet) -> this.findOne(id).onSuccess(promise::complete).onFailure(promise::fail));
//        });
        return null;
    }


    public Future<Void> delete(String id) {
        return this.checkExisted(id).compose(found -> {
            final String sql = "DELETE FROM API_SERVICE WHERE API_SERVICE_ID = $1";
            return this.execute(sql, Tuple.of(id), (promise, rowSet) -> promise.complete());
        });
    }

    public Future<Boolean> checkExisted(String id) {
        final String sql = "SELECT count(1) as result FROM API_SERVICE WHERE API_SERVICE_ID = $1";
        return this.existed(id, sql);
    }
}
