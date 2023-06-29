package com.rory.apimock.dao;

import cn.hutool.core.util.IdUtil;
import com.rory.apimock.dto.web.APICategory;
import com.rory.apimock.exceptions.ResourceNotFoundException;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.ArrayTuple;
import org.jooq.Record;
import org.jooq.UpdateConditionStep;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

public class APICategoryDao extends BaseDao<APICategory> {
    public APICategoryDao(SqlClient sqlClient) {
        super(sqlClient);
    }

    public Future<APICategory> save(APICategory dto) {

        final String sql = "INSERT INTO API_CATEGORY(API_CATEGORY_ID, NAME, DESCRIPTION, CREATE_AT, UPDATE_AT) VALUES($1, $2, $3, $4, $5)";
        OffsetDateTime now = currentUTCTime();
        dto.setId(IdUtil.fastSimpleUUID());
        return this.execute(sql,
            Tuple.from(Arrays.asList(dto.getId(),
                dto.getName(),
                dto.getDescription(),
                now,
                now)),
            (promise, rowSet) -> {
                dto.setCreateAt(formatToString(now));
                dto.setUpdateAt(formatToString(now));
                promise.complete(dto);
            });
    }

    public Future<APICategory> update(String id, APICategory dto) {
        return this.checkExisted(id).compose(found -> {
            //FIXME: Optional fields update?? dynamic SQL, HOW??
            OffsetDateTime now = currentUTCTime();
            final String sql = "UPDATE API_CATEGORY SET NAME = $1, UPDATE_AT = $3 WHERE API_CATEGORY_ID = $4";
            return this.execute(sql,
                Tuple.from(Arrays.asList(dto.getName(), dto.getDescription(), now, id)),
                (promise, rowSet) -> this.findOne(id).onSuccess(promise::complete).onFailure(promise::fail));
        });
    }

    public Future<Void> delete(String id) {
        // TODO: Check is API services under this category, if yes, delete is not allowed.
        return this.checkExisted(id).compose(found -> {
            final String sql = "DELETE FROM API_CATEGORY WHERE API_CATEGORY_ID = $1";
            return this.execute(sql, Tuple.of(id), (promise, rowSet) -> promise.complete());
        });
    }

    public Future<Boolean> checkExisted(String id) {
        final String sql = "SELECT count(1) as result FROM API_CATEGORY WHERE API_CATEGORY_ID = $1";
        return this.existed(id, sql);
    }

    public Future<APICategory> findOne(String id) {
        final String sql = "SELECT API_CATEGORY_ID, NAME, DESCRIPTION, CREATE_AT, UPDATE_AT FROM API_CATEGORY WHERE API_CATEGORY_ID = $1";
        return this.executeWithCollectorMapping(sql, Tuple.of(id), (promise, sqlResult) -> {
            Optional<APICategory> any = sqlResult.value().stream().findAny();
            if (any.isPresent()) {
                promise.complete(any.get());
            } else {
                promise.fail(new ResourceNotFoundException());
            }
        });
    }

    public Future<List<APICategory>> findAll() {
        final String sql = "SELECT API_CATEGORY_ID, NAME, DESCRIPTION, CREATE_AT, UPDATE_AT FROM API_CATEGORY";
        return this.executeWithCollectorMapping(sql, ArrayTuple.EMPTY, (promise, sqlResult) -> promise.complete(sqlResult.value()));
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

    public static void main(String[] args) {
        UpdateConditionStep<Record> where = DSL.update(table("API_CATEGORY"))
            .set(field("name"), "1")
            .set(field("description"), "abc")
            .where(condition(field("id").eq("123")));
        String sql = where.getSQL(ParamType.INLINED);
        System.out.println(sql);
    }
}
