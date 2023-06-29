package com.rory.apimock.utils;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigUtil {

    private static final String CONFIG_FILE_NAME = "application.yaml";

    private final Vertx vertx;

    public ConfigUtil(Vertx vertx) {
        this.vertx = vertx;
    }

    public static ConfigUtil getInstance(Vertx vertx) {
        return new ConfigUtil(vertx);
    }

    public Future<JsonObject> loadApplicationConfig() {
        Promise<JsonObject> promise = Promise.promise();
        ConfigStoreOptions storeOptions = new ConfigStoreOptions();
        storeOptions.setType("file").setFormat("yaml")
            .setConfig(new JsonObject().put("path", CONFIG_FILE_NAME));

        ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(storeOptions)).getConfig()
            .onSuccess(value -> {
                log.info("Configuration loading completed, {}", value);
                promise.complete(value);
            }).onFailure(promise::fail);
        return promise.future();
    }


}
