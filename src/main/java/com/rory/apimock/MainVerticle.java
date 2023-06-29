package com.rory.apimock;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rory.apimock.utils.ConfigUtil;
import com.rory.apimock.verticles.MockVerticle;
import com.rory.apimock.verticles.WebAPIVerticle;
import io.vertx.core.*;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deploy all Verticles
 */
@Slf4j
public class MainVerticle extends AbstractVerticle {


    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.configureJackson();
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        ConfigUtil.getInstance(vertx).loadApplicationConfig()
            .onFailure(startPromise::fail)
                .compose(result ->
                    this.deployVerticles(Arrays.asList(WebAPIVerticle.class, MockVerticle.class),
                        new DeploymentOptions().setConfig(result)))
                    .onSuccess(res -> {
                        log.info("Application started");
                        startPromise.complete();
                    })
                    .onFailure(startPromise::fail);
    }

    private void configureJackson() {
        ObjectMapper objectMapper = DatabindCodec.mapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }


    private Future<Void> deployVerticles(List<Class<? extends Verticle>> verticles, DeploymentOptions deploymentOptions) {
        log.info("Start to deploy Verticles ---> " + verticles);
        Promise<Void> promise = Promise.promise();
        List<Future<String>> futures = verticles.stream()
            .map(verticle -> vertx.deployVerticle(verticle.getName(), deploymentOptions))
            .collect(Collectors.toList());

        Future.all(futures).onSuccess(event -> {
            promise.complete();
        }).onFailure(promise::fail);

        return promise.future();
    }

    public static void main(String[] args) {
        VertxOptions vertxOptions = new VertxOptions();
        Vertx.vertx(vertxOptions).deployVerticle(MainVerticle.class.getName());
    }

}
