package com.rory.apimock.dao;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.rory.apimock.db.tables.records.ApiServiceRecord;
import com.rory.apimock.dto.web.APIService;
import com.rory.apimock.exceptions.ResourceNotFoundException;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;
import org.jooq.UpdateSetMoreStep;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.rory.apimock.db.Tables.API_CATEGORY;
import static com.rory.apimock.db.Tables.API_SERVICE;

@Slf4j
public class APIServiceDao extends BaseDao<APIService> {

    private final APICategoryDao categoryDao;
    private final APIPathStubDao pathStubDao;

    public APIServiceDao(SqlClient sqlClient) {
        super(sqlClient);
        this.categoryDao = new APICategoryDao(sqlClient);
        this.pathStubDao = new APIPathStubDao(sqlClient);
    }

    public Future<APIService> save(APIService dto) {
        return categoryDao.findOne(dto.getCategoryId()).compose(existed -> {
            dto.setId(IdUtil.fastSimpleUUID());
            dto.setCategoryId(existed.getId());
            dto.setCategoryName(existed.getName());
            OffsetDateTime now = currentUTCTime();

            final String sql = dslContext.insertInto(API_SERVICE, API_SERVICE.API_SERVICE_ID, API_SERVICE.NAME, API_SERVICE.CATEGORY_ID,
                    API_SERVICE.DESCRIPTION, API_SERVICE.PREFIX, API_SERVICE.VERSION, API_SERVICE.CREATE_AT, API_SERVICE.UPDATE_AT)
                .values(dto.getId(), dto.getName(), dto.getCategoryId(), dto.getDescription(), dto.getPrefix(),
                    dto.getVersion(), now, now).getSQL();

            return this.execute(sql,
                (promise, rowSet) -> {
                    dto.setCreateAt(formatToString(now));
                    dto.setUpdateAt(formatToString(now));
                    promise.complete(dto);
                });
        });
    }

    public Future<APIService> update(String id, APIService dto) {
        return this.checkExisted(id).compose(found -> {
            final OffsetDateTime now = currentUTCTime();
            UpdateSetMoreStep<ApiServiceRecord> updateStep = dslContext.update(API_SERVICE)
                .set(API_SERVICE.NAME, dto.getName())
                .set(API_SERVICE.CATEGORY_ID, dto.getCategoryId())
                .set(API_SERVICE.PREFIX, dto.getPrefix())
                .set(API_SERVICE.VERSION, dto.getVersion())
                .set(API_SERVICE.UPDATE_AT, now);
            if (StrUtil.isNotEmpty(dto.getDescription())) {
                updateStep.set(API_SERVICE.DESCRIPTION, dto.getDescription());
            }
            final String sql = updateStep.where(API_SERVICE.API_SERVICE_ID.eq(id)).getSQL();
            return this.execute(sql, (promise, rowSet) -> {
                dto.setUpdateAt(formatToString(now));
                promise.complete(dto);
            });
        });
    }

    public Future<List<APIService>> findAll() {
        final String sql = dslContext.select(API_SERVICE.API_SERVICE_ID, API_SERVICE.NAME, API_SERVICE.CATEGORY_ID, API_CATEGORY.NAME.as("category_name"),
                API_SERVICE.DESCRIPTION, API_SERVICE.PREFIX, API_SERVICE.VERSION, API_SERVICE.CREATE_AT, API_SERVICE.UPDATE_AT)
            .from(API_SERVICE).join(API_CATEGORY).on(API_SERVICE.CATEGORY_ID.eq(API_CATEGORY.API_CATEGORY_ID)).getSQL();
        return this.executeWithCollectorMapping(sql, (promise, listSqlResult) -> promise.complete(listSqlResult.value()));
    }

    public Future<APIService> findOne(String id) {
        final String sql = dslContext.select(API_SERVICE.API_SERVICE_ID, API_SERVICE.NAME, API_SERVICE.CATEGORY_ID, API_CATEGORY.NAME.as("category_name"),
                API_SERVICE.DESCRIPTION, API_SERVICE.PREFIX, API_SERVICE.VERSION, API_SERVICE.CREATE_AT, API_SERVICE.UPDATE_AT)
            .from(API_SERVICE).join(API_CATEGORY).on(API_SERVICE.CATEGORY_ID.eq(API_CATEGORY.API_CATEGORY_ID)).where(API_SERVICE.API_SERVICE_ID.eq(id)).getSQL();
        return this.executeWithCollectorMapping(sql, (promise, listSqlResult) ->
            listSqlResult.value().stream().findAny().ifPresentOrElse(any ->
                    pathStubDao.findByServiceId(any.getId())
                        .onSuccess(pathDefinitions -> {
                            any.setPathStubs(pathDefinitions);
                            promise.complete(any);
                        })
                        .onFailure(promise::fail),
                () -> promise.fail(new ResourceNotFoundException()))
        );
    }


    public Future<Void> delete(String id) {
        return this.checkExisted(id).compose(found -> {
            final String sql = dslContext.delete(API_SERVICE).where(API_SERVICE.API_SERVICE_ID.eq(id)).getSQL();
            return this.execute(sql, (promise, rowSet) -> pathStubDao.deleteByServiceId(id)
                .onSuccess(promise::complete)
                .onFailure(promise::fail));
        });
    }

    public Future<Void> checkExisted(String id) {
        final String sql = dslContext.selectOne().from(API_SERVICE).where(API_SERVICE.API_SERVICE_ID.eq(id)).getSQL();
        return this.existed(sql);
    }

    @Override
    protected Collector<Row, ?, List<APIService>> rowMappingCollector() {
        return Collectors.mapping(
            row -> new APIService(row.getString(API_SERVICE.API_SERVICE_ID.getName().toLowerCase()),
                formatToString(row.getOffsetDateTime(API_SERVICE.CREATE_AT.getName().toLowerCase())),
                formatToString(row.getOffsetDateTime(API_SERVICE.UPDATE_AT.getName().toLowerCase())),
                row.getString(API_SERVICE.NAME.getName().toLowerCase()),
                row.getString(API_SERVICE.CATEGORY_ID.getName().toLowerCase()),
                row.getString("category_name"),
                row.getString(API_SERVICE.DESCRIPTION.getName().toLowerCase()),
                row.getString(API_SERVICE.PREFIX.getName().toLowerCase()),
                row.getString(API_SERVICE.VERSION.getName().toLowerCase())),
            Collectors.toList()
        );
    }
}
