package com.rory.apimock.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

public abstract class BaseVerticle extends AbstractVerticle {

    protected SqlClient sqlClient;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.initDbConnection();
    }

    private void initDbConnection() {
        JsonObject dbObject = (JsonObject) JsonPointer.from("/db").queryJson(config());
        PgConnectOptions connectOptions = new PgConnectOptions()
            .setPort(dbObject.getInteger("port", 5432))
            .setHost(dbObject.getString("host", "localhost"))
            .setDatabase(dbObject.getString("database"))
            .setUser(dbObject.getString("username"))
            .setPassword(dbObject.getString("password"));
        PoolOptions poolOptions = new PoolOptions()
            .setConnectionTimeout(dbObject.getInteger("connectionTimeout"))
            .setIdleTimeout(dbObject.getInteger("idleTimeout"))
            .setMaxSize(10);
        this.sqlClient = PgPool.pool(vertx, connectOptions, poolOptions);
    }

    protected Future<HttpServer> createAndStartHttpServer(Router rootRouter, Integer port) {
        return vertx.createHttpServer()
            .requestHandler(rootRouter)
            .listen(port);
    }
}
