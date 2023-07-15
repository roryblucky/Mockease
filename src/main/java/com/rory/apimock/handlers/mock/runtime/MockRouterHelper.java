package com.rory.apimock.handlers.mock.runtime;

import com.github.jknack.handlebars.Template;
import com.rory.apimock.dto.APIStub;
import com.rory.apimock.utils.JsonPointerUtil;
import com.rory.apimock.utils.TemplateCacheUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.function.Predicate;

import static com.rory.apimock.dto.Constants.HTTPS_SCHEME;
import static com.rory.apimock.dto.Constants.IDENTIFIER;

@Slf4j
public class MockRouterHelper {

    private final TemplateCacheUtil<Template> templateCacheUtil;
    private final Router router;
    private final Vertx vertx;
    private final WebClient webClient;


    public MockRouterHelper(Vertx vertx, Router router) {
        this.vertx = vertx;
        this.router = router;
        templateCacheUtil = new TemplateCacheUtil<>(vertx);
        webClient = this.initWebClient();
    }

    private WebClient initWebClient() {
        WebClientOptions options = new WebClientOptions()
            .setUserAgent("Mockease-Webhook")
            .setKeepAlive(true)
            .setKeepAliveTimeout(JsonPointerUtil.queryJsonOrDefault("/proxy/keepAliveTimeout", vertx.getOrCreateContext().config(), 60000))
            .setConnectTimeout(JsonPointerUtil.queryJsonOrDefault("/webhook/connectionTimeout", vertx.getOrCreateContext().config(), 30000))
            .setIdleTimeout(JsonPointerUtil.queryJsonOrDefault("/webhook/idleTimeout", vertx.getOrCreateContext().config(), 30000))
            .setTryUseCompression(true)
            .setFollowRedirects(true)
            .setTrustAll(true)
            .setVerifyHost(false);

        return WebClient.create(vertx, options);
    }

    public void removeAllRoutesOnService(String serviceId) {
        router.getRoutes().stream()
            .filter(route -> route.<String>getMetadata(IDENTIFIER).contains(serviceId))
            .forEach(route -> {
                final String identifier = route.getMetadata(IDENTIFIER);
                log.info("Removing route: {}", identifier);
                route.remove();
                templateCacheUtil.removeKeyIfPresent(identifier);
            });
    }

    public void removeRoute(APIStub apiStub) {
        router.getRoutes().stream()
            .filter(unique(apiStub.getIdentifier()))
            .forEach(Route::remove);
        templateCacheUtil.removeKeyIfPresent(apiStub.getIdentifier());
    }

    private Predicate<Route> unique(String identifier) {
        return route -> route.getMetadata(IDENTIFIER).equals(identifier);
    }

    public Route createRoute(APIStub apiStub) {
        Route newRoute = router.route(HttpMethod.valueOf(apiStub.getMethod()), apiStub.getWholeUrl())
            .putMetadata(IDENTIFIER, apiStub.getIdentifier());
        this.configContentType(apiStub, newRoute);

        if (apiStub.isProxyEnabled()) {
            newRoute.handler(new MockProxyHandler(vertx, apiStub));
        } else {
            newRoute.handler(new DynamicResponseHandler(vertx, apiStub));
            newRoute.handler(new StaticResponseHandler(vertx, apiStub));
        }

        return newRoute;
    }


    public void fireWebhook(APIStub apiStub) {
        log.info("Webhook fired for: {}", apiStub.getIdentifier());
        URI uri = URI.create(apiStub.getWebhookUrl());

        HttpRequest<Buffer> request = webClient.requestAbs(HttpMethod.valueOf(apiStub.getWebhookMethod()), apiStub.getWebhookUrl())
            .putHeaders(HeadersMultiMap.headers().addAll(apiStub.getWebhookHeaders()))
            .ssl(HTTPS_SCHEME.equals(uri.getScheme()));
        Handler<AsyncResult<HttpResponse<Buffer>>>  responseHandler = ar -> {
            if (ar.succeeded()) {
                log.info("Webhook fired successfully for: {}, response: {}", apiStub.getIdentifier(), ar.result().bodyAsString());
            } else {
                log.error("Webhook failed for: {}", apiStub.getIdentifier(), ar.cause());
            }
        };

        if (HttpMethod.POST.name().equalsIgnoreCase(apiStub.getWebhookMethod()) || HttpMethod.PUT.name().equalsIgnoreCase(apiStub.getWebhookMethod())) {
            request.sendBuffer(Buffer.buffer(apiStub.getWebhookBody()), responseHandler);
        } else {
            request.send(responseHandler);
        }
    }


    private void configContentType(APIStub apiStub, Route newRoute) {
        final String requestContentType = apiStub.getRequestContentType();
        final String responseContentType = apiStub.getResponseHeaders().get(HttpHeaders.CONTENT_TYPE.toString());
        if (requestContentType != null) {
            newRoute.consumes(requestContentType);
        }

        if (responseContentType != null) {
            newRoute.produces(responseContentType);
        }
    }
}
