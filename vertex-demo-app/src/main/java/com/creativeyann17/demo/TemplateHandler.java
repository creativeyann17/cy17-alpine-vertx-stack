package com.creativeyann17.demo;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;

public class TemplateHandler implements Handler<RoutingContext> {

  private TemplateEngine templateEngine;
  private String templatesFolder = "templates";

  public TemplateHandler(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
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
    templateEngine.render(context, templatesFolder + "/" + path).onComplete(handler -> {
      if (handler.succeeded()) {
        routingContext.response().end(handler.result());
      } else {
        routingContext.fail(handler.cause());
      }
    });
  }
}
