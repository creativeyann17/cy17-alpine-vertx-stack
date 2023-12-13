package com.creativeyann17.demo.handlers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedirectHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext rc) {
    log.warn("Redirect: {} from: {}", rc.request().remoteAddress(), rc.request().absoluteURI());
    HttpServerResponse response = rc.response();
    response.setStatusCode(303);
    response.headers().add("Location", "/");
    response.end();
  }
}
