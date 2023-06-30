package com.rory.apimock.dao;

import cn.hutool.core.util.IdUtil;
import com.rory.apimock.dto.web.ResponseInfo;
import com.rory.apimock.exceptions.ResourceNotFoundException;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.rory.apimock.db.Tables.API_PROXY;

public class APIProxyDao extends BaseDao<ResponseInfo.Proxy> {

    protected APIProxyDao(SqlClient sqlClient) {
        super(sqlClient);
    }

    public Future<ResponseInfo.Proxy> save(ResponseInfo.Proxy dto) {
        OffsetDateTime now = currentUTCTime();
        dto.setId(IdUtil.fastSimpleUUID());
        final String sql = dslContext.insertInto(API_PROXY,
                API_PROXY.API_PROXY_ID, API_PROXY.URL, API_PROXY.CREATE_AT, API_PROXY.UPDATE_AT)
            .values(dto.getId(), dto.getUrl(), now, now).getSQL();
        return this.execute(sql, (promise, rowSet) -> promise.complete(dto));
    }

    public Future<Void> delete(String id) {
        final String sql = dslContext.delete(API_PROXY).where(API_PROXY.API_PROXY_ID.eq(id)).getSQL();
        return this.execute(sql, (promise, rowSet) -> promise.complete());
    }

    public Future<ResponseInfo.Proxy> update(String id, ResponseInfo.Proxy dto) {
        final String sql = dslContext.update(API_PROXY).set(API_PROXY.URL, dto.getUrl())
            .set(API_PROXY.UPDATE_AT, currentUTCTime()).where(API_PROXY.API_PROXY_ID.eq(id)).getSQL();
        return this.execute(sql, (proxyPromise, rowSet) -> proxyPromise.complete(dto));
    }

    public Future<ResponseInfo.Proxy> findOne(String id) {
        final String sql = dslContext.select(API_PROXY.URL, API_PROXY.CREATE_AT, API_PROXY.UPDATE_AT).where(API_PROXY.API_PROXY_ID.eq(id)).getSQL();
        return this.executeWithCollectorMapping(sql, (proxyPromise, listSqlResult) -> {
            Optional<ResponseInfo.Proxy> any = listSqlResult.value().stream().findAny();
            if (any.isPresent()) {
                proxyPromise.complete(any.get());
            } else {
                proxyPromise.fail(new ResourceNotFoundException());
            }
        });
    }

    @Override
    protected Collector<Row, ?, List<ResponseInfo.Proxy>> rowMappingCollector() {
        return Collectors.mapping(
            row -> new ResponseInfo.Proxy(row.getString("api_proxy_id"),
                formatToString(row.getOffsetDateTime("url"))),
            Collectors.toList()
        );
    }
}
