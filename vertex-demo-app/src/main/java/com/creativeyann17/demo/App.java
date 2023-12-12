package com.creativeyann17.demo;

import ch.qos.logback.classic.Level;
import io.vertx.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import org.apache.commons.lang3.StringUtils;

import javax.swing.text.html.Option;
import java.util.Optional;

public class App extends AbstractVerticle {

  final static String UUID = java.util.UUID.randomUUID().toString();
  final static String ENV = Optional.ofNullable(System.getenv("ENV")).filter(StringUtils::isNotBlank).orElse("prod");
  final static Integer PORT = Integer.valueOf(System.getenv("PORT"));

  static {
    System.setProperty("vertxweb.environment", ENV);
    if (System.getProperty("org.vertx.logger-delegate-factory-class-name") == null) {
      System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
    }
    final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    root.setLevel("prod".equals(ENV) ? Level.ERROR: Level.INFO);
  }

  private static final Logger log = LoggerFactory.getLogger(App.class);

  public static void main(String[]args) {
    log.info("App is starting with Cores: " + Runtime.getRuntime().availableProcessors());
    log.info("Current profile: " + ENV);

    var options = new VertxOptions();

    if ("prod".equals(ENV)) {
      var clusterManager = new InfinispanClusterManager();
      options.setClusterManager(clusterManager);
      handleStartingFailure(Vertx.clusteredVertx(options)
        .onSuccess(App::startVerticles));
    } else {
      Vertx vertx = Vertx.vertx(new VertxOptions());
      startVerticles(vertx);
    }
  }

  private static void startVerticles(Vertx vertx) {
    handleStartingFailure(vertx.deployVerticle(new WebVerticle()));
    handleStartingFailure(vertx.deployVerticle(new RequestHandler(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)));
  }

  private static void handleStartingFailure(Future<?> verticle) {
    verticle.onFailure(failure -> {
      log.error("", failure);
      System.exit(-1);
    });
  }
}
