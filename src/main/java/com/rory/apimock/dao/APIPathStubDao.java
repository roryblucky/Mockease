package com.rory.apimock.dao;

import cn.hutool.core.util.IdUtil;
import com.rory.apimock.dto.web.APIPathDefinition;
import com.rory.apimock.dto.web.ResponseInfo;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collector;

import static com.rory.apimock.db.Tables.API_PATH_STUB;

public class APIPathStubDao extends BaseDao<APIPathDefinition> {

    private final APIWebhookDao webhookDao;

    private final APIProxyDao proxyDao;
    public APIPathStubDao(SqlClient sqlClient) {
        super(sqlClient);
        this.proxyDao = new APIProxyDao(sqlClient);
        this.webhookDao = new APIWebhookDao(sqlClient);
    }

    public Future<APIPathDefinition> save(String serviceId, APIPathDefinition dto) {
        return Future.all(saveWebhookInfo(dto), saveProxyInfo(dto)).compose(compositeFuture -> {
            ResponseInfo.WebhookInfo webhookInfo = compositeFuture.resultAt(0);
            ResponseInfo.Proxy proxy = compositeFuture.resultAt(1);

            dto.setId(IdUtil.fastSimpleUUID());
            OffsetDateTime now = currentUTCTime();
            final String sql = dslContext
                .insertInto(API_PATH_STUB, API_PATH_STUB.API_PATH_STUB_ID, API_PATH_STUB.API_SERVICE_ID, API_PATH_STUB.NAME,
                    API_PATH_STUB.DESCRIPTION, API_PATH_STUB.OPERATION_ID, API_PATH_STUB.PATH, API_PATH_STUB.METHOD, API_PATH_STUB.REQUEST_HEADERS,
                    API_PATH_STUB.VALIDATION_ENABLED, API_PATH_STUB.REQUEST_SCHEMA, API_PATH_STUB.REQUEST_DYNAMIC_BODY, API_PATH_STUB.REQUEST_BODY,
                    API_PATH_STUB.RESPONSE_HTTP_STATUS, API_PATH_STUB.RESPONSE_HEADERS, API_PATH_STUB.RESPONSE_DYNAMIC_BODY, API_PATH_STUB.RESPONSE_BODY,
                    API_PATH_STUB.API_WEBHOOK_ID, API_PATH_STUB.API_PROXY_ID, API_PATH_STUB.CREATE_AT, API_PATH_STUB.UPDATE_AT)
                .values(dto.getId(), serviceId, dto.getName(), dto.getDescription(), dto.getOperationId(), dto.getRequest().getPath(),
                    dto.getRequest().getMethod(), new JsonObject(dto.getRequest().getHeaders()).encode(), dto.getRequest().isValidationEnabled(),
                    dto.getRequest().getRequestSchema(), dto.getRequest().isDynamicBody(), dto.getRequest().getBody(), dto.getResponse().getHttpStatus(),
                    new JsonObject(dto.getResponse().getHeaders()).encode(), dto.getResponse().isDynamicBody(), dto.getResponse().getBody(), webhookInfo.getId(),
                    proxy.getId(), now, now)
                .getSQL();
            return this.execute(sql, (promise, rowSet) -> {
                dto.setCreateAt(formatToString(now));
                dto.setUpdateAt(formatToString(now));
                promise.complete(dto);
            });
        });
    }

    private Future<ResponseInfo.Proxy> saveProxyInfo(APIPathDefinition dto) {
        ResponseInfo.Proxy proxyDto = dto.getResponse().getProxy();
        if (proxyDto != null) {
            return proxyDao.save(proxyDto);
        }
        return Future.succeededFuture(new ResponseInfo.Proxy());
    }

    private Future<ResponseInfo.WebhookInfo> saveWebhookInfo(APIPathDefinition dto) {
        ResponseInfo.WebhookInfo webhook = dto.getResponse().getWebhook();
        if (webhook != null) {
            return webhookDao.save(webhook);
        }
        return Future.succeededFuture(new ResponseInfo.WebhookInfo());
    }


    @Override
    protected Collector<Row, ?, List<APIPathDefinition>> rowMappingCollector() {
        return null;
    }
}
