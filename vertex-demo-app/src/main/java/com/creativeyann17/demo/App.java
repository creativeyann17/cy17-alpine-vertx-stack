package com.creativeyann17.demo;

import ch.qos.logback.classic.Level;
import io.vertx.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;

public class App extends AbstractVerticle {

  final static String UUID = java.util.UUID.randomUUID().toString();

  static {
    String logFactory = System.getProperty("org.vertx.logger-delegate-factory-class-name");
    if (logFactory == null) {
      System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
    }
    var env = System.getenv("ENV");
    final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    root.setLevel("prod".equals(env) ? Level.ERROR: Level.INFO);
  }

  private static final Logger log = LoggerFactory.getLogger(App.class);

  public static void main(String[]args) {
    log.info("App starting...");

    var mgr = new InfinispanClusterManager();

    var options = new VertxOptions()
      .setClusterManager(mgr);

    Vertx.clusteredVertx(options)
      .onSuccess(vertx -> {
        vertx.deployVerticle(new WebVerticle());
        vertx.deployVerticle(new RequestHandler(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD));
      }).onFailure(failure -> {
        throw new RuntimeException(failure);
      });
  }
}
