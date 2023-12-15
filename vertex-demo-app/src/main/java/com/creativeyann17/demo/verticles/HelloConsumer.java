package com.creativeyann17.demo.verticles;

import com.creativeyann17.demo.utils.Configuration;
import com.creativeyann17.demo.utils.SingletonsContext;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloConsumer extends AbstractVerticle {

  public static final String HELLO_EVENT = "HELLO_EVENT";

  private HealthChecks healthChecks;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    this.healthChecks = SingletonsContext.get(HealthChecks.class);
  }

  @Override
  public void start(Promise<Void> startPromise) {
    var consumer = vertx.eventBus().consumer(HELLO_EVENT, message -> {
      log.info(String.format("[%s] %s", Configuration.UUID, message.body()));
      message.reply("Hello World !!!");
    });
    if (consumer.isRegistered()) {
      startPromise.complete();
      healthChecks.register("hello-worker", statusPromise -> statusPromise.complete(Status.OK()));
    } else {
      startPromise.fail("Cant create consumer " + HELLO_EVENT);
      healthChecks.register("hello-worker", statusPromise -> statusPromise.complete(Status.KO()));
    }
  }
}
