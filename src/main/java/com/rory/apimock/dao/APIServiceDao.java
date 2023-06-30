package com.rory.apimock.dao;

import cn.hutool.core.util.IdUtil;
import com.rory.apimock.dto.web.APIService;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.rory.apimock.db.Tables.API_CATEGORY;
import static com.rory.apimock.db.Tables.API_SERVICE;

@Slf4j
public class APIServiceDao extends BaseDao<APIService> {

    private final APICategoryDao categoryDao;

    public APIServiceDao(SqlClient sqlClient) {
        super(sqlClient);
        this.categoryDao = new APICategoryDao(sqlClient);
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
//        return this.checkExisted(id).compose(found -> {
//            final OffsetDateTime now = currentUTCTime();
//            final String sql = "UPDATE API_SERVICE SET NAME = $1, DESCRIPTION = $2, PREFIX = $3, VERSION = $4, , CATEGORY_ID = $5, UPDATE_AT = $6 WHERE API_SERVICE_ID = $7";
//            return this.execute(sql,
//                Tuple.from(Arrays.asList(dto.getName(), dto.getDescription(), dto.getPrefix(), dto.getDescription(), now, id)),
//                (promise, rowSet) -> this.findOne(id).onSuccess(promise::complete).onFailure(promise::fail));
//        });
        return null;
    }

    public Future<List<APIService>> findAll() {
        final String sql = dslContext.select(API_SERVICE.API_SERVICE_ID, API_SERVICE.NAME, API_SERVICE.CATEGORY_ID, API_CATEGORY.NAME.as("category_name"),
                API_SERVICE.DESCRIPTION, API_SERVICE.PREFIX, API_SERVICE.VERSION, API_SERVICE.CREATE_AT, API_SERVICE.UPDATE_AT)
            .from(API_SERVICE).join(API_CATEGORY).on(API_SERVICE.CATEGORY_ID.eq(API_CATEGORY.API_CATEGORY_ID)).getSQL();
        return this.executeWithCollectorMapping(sql, (promise, listSqlResult) -> promise.complete(listSqlResult.value()));
    }




    public Future<Void> delete(String id) {
        return this.checkExisted(id).compose(found -> {
            final String sql = dslContext.delete(API_SERVICE).where(API_SERVICE.API_SERVICE_ID.eq(id)).getSQL();
            return this.execute(sql, (promise, rowSet) -> promise.complete());
        });
    }

    public Future<Void> checkExisted(String id) {
        final String sql = dslContext.selectOne().from(API_SERVICE).where(API_SERVICE.API_SERVICE_ID.eq(id)).getSQL();
        return this.existed(sql);
    }

    @Override
    protected Collector<Row, ?, List<APIService>> rowCollector() {
        return Collectors.mapping(
            row -> new APIService(row.getString("api_service_id"),
                formatToString(row.getOffsetDateTime("create_at")),
                formatToString(row.getOffsetDateTime("update_at")),
                row.getString("name"),
                row.getString("category_id"),
                row.getString("category_name"),
                row.getString("description"),
                row.getString("prefix"),
                row.getString("version")),
            Collectors.toList()
        );
    }
}
