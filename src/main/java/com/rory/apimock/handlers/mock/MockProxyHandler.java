package com.rory.apimock.handlers.mock;

import com.rory.apimock.dto.APIStub;
import com.rory.apimock.utils.JsonPointerUtil;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.proxy.handler.ProxyHandler;
import io.vertx.httpproxy.*;

import java.net.URI;

import static com.rory.apimock.dto.Constants.API_MOCK_ENDPOINT_PREFIX;
import static com.rory.apimock.dto.Constants.HTTPS_SCHEME;

public class MockProxyHandler implements ProxyInterceptor, Handler<RoutingContext> {

    private final APIStub apiStub;
    private final ProxyHandler delegate;


    public MockProxyHandler(Vertx vertx, APIStub apiStub) {
        this.apiStub = apiStub;
        this.delegate = this.initProxyHandler(vertx);
    }

    private ProxyHandler initProxyHandler(Vertx vertx) {
        final URI uri = URI.create(apiStub.getProxyHost());

        final HttpClientOptions options = new HttpClientOptions()
            .setSsl(HTTPS_SCHEME.equalsIgnoreCase(uri.getScheme()))
            .setConnectTimeout(JsonPointerUtil.queryJsonOrDefault("/proxy/connectionTimeout", vertx.getOrCreateContext().config(), 30000))
            .setKeepAliveTimeout(JsonPointerUtil.queryJsonOrDefault("/proxy/keepAliveTimeout", vertx.getOrCreateContext().config(), 60000))
            .setTrustAll(true);

        HttpProxy proxy = HttpProxy.reverseProxy(vertx.createHttpClient(options));
        proxy.addInterceptor(this);
        proxy.origin(apiStub.getProxyPort(), uri.getHost());

        return ProxyHandler.create(proxy);
    }


    @Override
    public void handle(RoutingContext ctx) {
        delegate.handle(ctx);
    }

    @Override
    public Future<ProxyResponse> handleProxyRequest(ProxyContext context) {
        ProxyRequest request = context.request();
        String newUrl = request.getURI()
            .replaceAll(API_MOCK_ENDPOINT_PREFIX + "/" + apiStub.getVersion() + apiStub.getBasePath(), "");
        request.setURI(newUrl);
        return ProxyInterceptor.super.handleProxyRequest(context);
    }


}
