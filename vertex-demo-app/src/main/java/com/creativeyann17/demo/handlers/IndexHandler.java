package com.creativeyann17.demo.handlers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IndexHandler implements Handler<RoutingContext> {

  private final TemplateHandler templateHandler;

  @Override
  public void handle(RoutingContext routingContext) {
    final JsonObject context = new JsonObject()
      .put("value", "foo");
    templateHandler.handle(routingContext, "index.html", context);
  }
}
