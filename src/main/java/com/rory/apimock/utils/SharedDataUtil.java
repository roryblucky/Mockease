package com.rory.apimock.utils;

import io.vertx.core.Vertx;

public class SharedDataUtil {

    private Vertx vertx;
    private SharedDataUtil() {
    }

    public static SharedDataUtil getInstance(Vertx vertx) {
        SingletonHolder.INSTANCE.vertx = vertx;
        return SingletonHolder.INSTANCE;
    }

    public Object get(String key) {
        return this.get(key, key);
    }

    public Object get(String mapName, String key) {
        return vertx.sharedData().getLocalMap(mapName).get(key);
    }
    public Object put(String key, Object value) {
        return this.put(key, key, value);
    }

    public Object put(String mapName, String key, Object value) {
        return vertx.sharedData().getLocalMap(mapName).put(key, value);
    }
    private static class SingletonHolder {
        public static final SharedDataUtil INSTANCE = new SharedDataUtil();
    }
}
