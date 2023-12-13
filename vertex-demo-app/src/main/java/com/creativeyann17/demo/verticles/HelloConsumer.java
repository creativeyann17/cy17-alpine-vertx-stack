package com.creativeyann17.demo.verticles;

import com.creativeyann17.demo.Configuration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloConsumer extends AbstractVerticle {

  public static final String HELLO_EVENT = "HELLO_EVENT";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var consumer = vertx.eventBus().consumer(HELLO_EVENT, message -> {
      log.info(String.format("[%s] %s", Configuration.UUID, message.body()));
      message.reply("Hello World!");
    });
    if (consumer.isRegistered()) {
      startPromise.complete();
    } else {
      startPromise.fail("Cant create consumer HELLO");
    }
  }
}
