package com.creativeyann17.demo.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalFailureHandler implements Handler<RoutingContext> {

  @Override
  public void handle(RoutingContext routingContext) {
    var exception = routingContext.failure();
    log.error("", exception);
    routingContext.response().setStatusCode(500).end("");
  }
}
