package com.creativeyann17.demo.handlers;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;

public class TemplateHandler implements Handler<RoutingContext> {

  private final TemplateEngine templateEngine;
  private final String root;

  public TemplateHandler(Vertx vertx, TemplateEngine templateEngine, String root) {
    this.templateEngine = templateEngine;
    this.root = root;
  }

  public void handle(RoutingContext routingContext, String path, JsonObject context) {
    routingContext.put("path", path);
    routingContext.put("context", context);
    handle(routingContext);
  }

  @Override
  public void handle(RoutingContext routingContext) {
    var path = routingContext.get("path").toString();
    var context = (JsonObject) routingContext.get("context");
    templateEngine.render(context, root + "/" + path).onComplete(handler -> {
      if (handler.succeeded()) {
        routingContext.response().end(handler.result());
      } else {
        routingContext.fail(handler.cause());
      }
    });
  }
}
