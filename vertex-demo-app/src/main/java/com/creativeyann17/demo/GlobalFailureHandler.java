package com.creativeyann17.demo;

import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class GlobalFailureHandler implements Handler<RoutingContext> {

  private static final Logger log = LoggerFactory.getLogger(GlobalFailureHandler.class);

  @Override
  public void handle(RoutingContext routingContext) {
    var exception = routingContext.failure();
    log.error(exception, exception);
    routingContext.response().setStatusCode(500).end("");
  }
}
