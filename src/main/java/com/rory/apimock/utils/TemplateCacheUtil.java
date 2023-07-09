package com.rory.apimock.utils;

import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.common.template.impl.TemplateHolder;

public class TemplateCacheUtil<T> {

    private final LocalMap<String, TemplateHolder<T>> cache;

    public TemplateCacheUtil(Vertx vertx) {
        cache = vertx.sharedData().getLocalMap("__vertx.web.template.cache");
    }

    public void removeKeyIfPresent(String key) {
        cache.remove(key);
    }

    public void clearCache() {
        cache.clear();
    }
}
