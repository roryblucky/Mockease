package com.rory.apimock.handlers.mock;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.rory.apimock.dto.APIStub;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.impl.TemplateHolder;
import io.vertx.ext.web.templ.handlebars.impl.HandlebarsTemplateEngineImpl;

import java.util.Map;

import static com.rory.apimock.dto.Constants.DYNAMIC_RESPONSE;

public class DynamicResponseHandler extends HandlebarsTemplateEngineImpl implements Handler<RoutingContext> {

    private final APIStub apiStub;
    private final Vertx vertx;
    private final Handlebars handlebars;

    public DynamicResponseHandler(Vertx vertx, APIStub apiStub) {
        super(vertx, "mock");
        this.vertx = vertx;
        this.apiStub = apiStub;
        this.handlebars = initHandlebars();
    }

    @Override
    public void handle(RoutingContext ctx) {

        if(apiStub.isResponseDynamicBodyEnabled()) {

            setVariables(ctx);

            this.render(ctx.data(), apiStub.getResponseBody(), res -> {
                if (res.succeeded()) {
                    if (!ctx.request().isEnded()) {
                        ctx.request().resume();
                    }

                    ctx.put(DYNAMIC_RESPONSE, res.result());
                    ctx.next();
                } else {
                    if (!ctx.request().isEnded()) {
                        ctx.request().resume();
                    }
                    ctx.fail(res.cause());
                }
            });
        } else {
            ctx.next();
        }

    }

    private Handlebars initHandlebars() {
        Handlebars handlebars = new Handlebars();
        handlebars.setStartDelimiter("${");
        handlebars.setEndDelimiter("}");
        return handlebars;
    }

    private void setVariables(RoutingContext ctx) {

        ctx.put("queryParams", toJson(ctx.queryParams()));

        ctx.put("pathParams", ctx.pathParams());

        ctx.put("headers", toJson(ctx.request().headers()));

        if (ctx.body().buffer() != null) {
            ctx.put("body", Json.decodeValue(ctx.body().buffer()));
        }
    }

    private static JsonObject toJson(MultiMap data) {
        JsonObject queryParams = new JsonObject();
        for (Map.Entry<String, String> entry : data.entries()) {
            queryParams.put(entry.getKey(), entry.getValue());
        }
        return queryParams;
    }


    @Override
    public void render(Map<String, Object> context, String contentStr, Handler<AsyncResult<Buffer>> handler) {
        try {
            String operationId = apiStub.getOperationId();
            TemplateHolder<Template> template = getTemplate(operationId);
            if (template == null) {
                synchronized (this) {
                    template = new TemplateHolder<>(handlebars.compile(new StringTemplateSource(operationId, contentStr)));
                }
                putTemplate(operationId, template);
            }
            Context engineContext = Context.newBuilder(context).resolver(getResolvers()).build();
            handler.handle(Future.succeededFuture(Buffer.buffer(template.template().apply(engineContext))));
        } catch (Exception ex) {
            handler.handle(Future.failedFuture(ex));
        }
    }

}
