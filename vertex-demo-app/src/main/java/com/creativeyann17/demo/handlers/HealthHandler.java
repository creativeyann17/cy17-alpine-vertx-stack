package com.creativeyann17.demo.handlers;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class HealthHandler implements Handler<RoutingContext> {

  private static final Status STATUS_OK = new Status("OK");

  @Override
  public void handle(RoutingContext rc) {
    rc.response().end(Json.encodePrettily(STATUS_OK));
  }

  private record Status(String status) {
  }

}
