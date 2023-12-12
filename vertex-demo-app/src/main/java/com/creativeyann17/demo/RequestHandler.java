package com.creativeyann17.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import static com.creativeyann17.demo.App.UUID;

public class RequestHandler extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var consumer = vertx.eventBus().consumer("REQUEST", message -> {
      log.info(String.format("[%s] %s", UUID, message.body()));
      message.reply("Hello World!");
    });
    if (consumer.isRegistered()) {
      startPromise.complete();
    } else {
      startPromise.fail("Cant create consumer REQUEST");
    }
  }
}
