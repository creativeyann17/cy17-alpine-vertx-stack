package com.creativeyann17.demo;

import com.creativeyann17.demo.verticles.HelloConsumer;
import com.creativeyann17.demo.verticles.WebServer;
import io.vertx.core.*;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class App extends AbstractVerticle {

  public static void main(String[] args) {

    Configuration.init();

    log.info("App is starting with Cores: {}", Runtime.getRuntime().availableProcessors());
    log.info("Current profile: {}", Configuration.env());

    var options = new VertxOptions();

    if (Configuration.isProd()) {
      var clusterManager = new InfinispanClusterManager();
      options.setClusterManager(clusterManager);
      handleStartingFailure(Vertx.clusteredVertx(options)
        .onSuccess(App::startVerticles));
    } else {
      Vertx vertx = Vertx.vertx(options);
      startVerticles(vertx);
    }
  }

  private static void startVerticles(Vertx vertx) {
    handleStartingFailure(
      vertx.deployVerticle(new WebServer()),
      vertx.deployVerticle(new HelloConsumer(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
    ;
  }

  private static void handleStartingFailure(Future<?>... verticles) {
    Future.all(Arrays.asList(verticles)).onFailure(failure -> {
      log.error("", failure);
      System.exit(-1);
    });
  }
}
