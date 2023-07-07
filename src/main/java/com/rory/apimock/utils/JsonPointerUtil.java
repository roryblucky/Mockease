package com.rory.apimock.utils;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

@SuppressWarnings("unchecked")
public class JsonPointerUtil {
    public static <T> T queryJsonOrDefault(String jsonPointer, JsonObject json, T defaultValue) {
        return (T) JsonPointer.from(jsonPointer).queryJsonOrDefault(json, defaultValue);
    }
}
