package com.rory.apimock.dao;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.rory.apimock.db.tables.records.ApiCategoryRecord;
import com.rory.apimock.dto.web.APICategory;
import com.rory.apimock.exceptions.ResourceNotFoundException;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.jooq.UpdateSetMoreStep;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.rory.apimock.db.Tables.API_CATEGORY;

@Slf4j
public class APICategoryDao extends BaseDao<APICategory> {
    public APICategoryDao(SqlClient sqlClient) {
        super(sqlClient);
    }

    public Future<APICategory> save(APICategory dto) {
        OffsetDateTime now = currentUTCTime();
        dto.setId(IdUtil.fastSimpleUUID());

        final String sql = dslContext
            .insertInto(API_CATEGORY, API_CATEGORY.API_CATEGORY_ID, API_CATEGORY.NAME,
                API_CATEGORY.DESCRIPTION, API_CATEGORY.UPDATE_AT, API_CATEGORY.CREATE_AT)
            .values(dto.getId(), dto.getName(), dto.getDescription(), now, now).getSQL();
        return this.execute(sql,
            (promise, rowSet) -> {
                dto.setCreateAt(formatToString(now));
                dto.setUpdateAt(formatToString(now));
                promise.complete(dto);
            });
    }

    public Future<APICategory> update(String id, APICategory dto) {
        return this.checkExisted(id).compose(found -> {
            OffsetDateTime now = currentUTCTime();

            UpdateSetMoreStep<ApiCategoryRecord> sqlStep = dslContext.update(API_CATEGORY)
                .set(API_CATEGORY.NAME, dto.getName())
                .set(API_CATEGORY.UPDATE_AT, now);
            if (StrUtil.isNotEmpty(dto.getDescription())) {
                sqlStep.set(API_CATEGORY.DESCRIPTION, dto.getDescription());
            }
            final String finalSQl = sqlStep.where(API_CATEGORY.API_CATEGORY_ID.eq(id)).getSQL();

            return this.execute(finalSQl, (promise, rowSet) -> this.findOne(id).onSuccess(promise::complete).onFailure(promise::fail));
        });
    }

    public Future<Void> delete(String id) {
        // TODO: Check is API services under this category, if yes, delete is not allowed.
        return this.checkExisted(id).compose(found -> {
            final String sql = "DELETE FROM API_CATEGORY WHERE API_CATEGORY_ID = $1";
            return this.execute(sql, Tuple.of(id), (promise, rowSet) -> promise.complete());
        });
    }

    public Future<Void> checkExisted(String id) {
        final String sql = dslContext.selectOne().from(API_CATEGORY).where(API_CATEGORY.API_CATEGORY_ID.eq(id)).getSQL();
        return this.existed(sql);
    }

    public Future<APICategory> findOne(String id) {
        final String sql = dslContext.select(API_CATEGORY.API_CATEGORY_ID, API_CATEGORY.NAME,
                API_CATEGORY.DESCRIPTION, API_CATEGORY.CREATE_AT, API_CATEGORY.UPDATE_AT)
            .from(API_CATEGORY)
            .where(API_CATEGORY.API_CATEGORY_ID.eq(id)).getSQL();

        return this.executeWithCollectorMapping(sql, (promise, sqlResult) -> {
            Optional<APICategory> any = sqlResult.value().stream().findAny();
            if (any.isPresent()) {
                promise.complete(any.get());
            } else {
                promise.fail(new ResourceNotFoundException());
            }
        });
    }

    public Future<List<APICategory>> findAll() {
        final String sql = dslContext.select(API_CATEGORY.API_CATEGORY_ID, API_CATEGORY.NAME,
                API_CATEGORY.DESCRIPTION, API_CATEGORY.CREATE_AT, API_CATEGORY.UPDATE_AT)
            .from(API_CATEGORY).getSQL();
        return this.executeWithCollectorMapping(sql, (promise, sqlResult) -> promise.complete(sqlResult.value()));
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
