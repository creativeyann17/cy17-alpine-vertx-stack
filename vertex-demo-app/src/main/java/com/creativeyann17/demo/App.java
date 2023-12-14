package com.creativeyann17.demo;

import com.creativeyann17.demo.verticles.HelloConsumer;
import com.creativeyann17.demo.verticles.WebServer;
import io.vertx.core.*;
import io.vertx.ext.cluster.infinispan.ClusterHealthCheck;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class App extends AbstractVerticle {

  private static final Map<String, Object> singletons = new HashMap<>();

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

  private static Future<HealthChecks> createHealthChecks(Vertx vertx) {
    return vertx.executeBlocking(() -> {  // a bit overkill but fun
      var healthChecks = HealthChecks.create(vertx);
      if (Configuration.isProd()) {
        Handler<Promise<Status>> procedure = ClusterHealthCheck.createProcedure(vertx, true);
        healthChecks.register("cluster-health", procedure);
      }
      return healthChecks;
    });
  }

  private static void startVerticles(Vertx vertx) {
    handleStartingFailure(createHealthChecks(vertx).onSuccess(healthChecks -> {
      putSingleton(HealthChecks.class, healthChecks);
      handleStartingFailure(
        vertx.deployVerticle(new WebServer()),
        vertx.deployVerticle(new HelloConsumer(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)));
    }));
  }

  private static void handleStartingFailure(Future<?>... verticles) {
    Future.all(Arrays.asList(verticles)).onFailure(failure -> {
      log.error("", failure);
      System.exit(-1);
    });
  }

  public synchronized static <T> T putSingleton(Class<?> c, T o) {
    return (T) singletons.put(c.getSimpleName(), o);
  }

  public synchronized static <T> T getSingleton(Class<T> c) {
    String name = c.getSimpleName();
    if (!singletons.containsKey(name))
      throw new RuntimeException("Unknown singleton: " + name);
    return (T) singletons.get(name);
  }
}
