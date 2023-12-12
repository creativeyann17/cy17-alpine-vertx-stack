package com.creativeyann17.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import static com.creativeyann17.demo.App.UUID;

public class WebVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(WebVerticle.class);

  final int PORT = Integer.parseInt(System.getenv("PORT"));

  @Override
  public void start(Promise<Void> startPromise) {
    vertx
      .createHttpServer()
      .requestHandler(req -> {
        vertx.eventBus().request("REQUEST", UUID).onSuccess(handler -> {
          req.response()
            .putHeader("content-type", "text/plain")
            .end(handler.body().toString());
        }).onFailure(failure -> {
            throw new RuntimeException(failure);
        });
      }).listen(PORT, http -> {
        if (http.succeeded()) {
          startPromise.complete();
          log.info("HTTP server started on port " + PORT);
        } else {
          startPromise.fail(http.cause());
        }
      });
  }
}
