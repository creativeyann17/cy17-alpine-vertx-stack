package com.creativeyann17.demo;

import com.creativeyann17.demo.utils.Configuration;
import com.creativeyann17.demo.utils.SingletonsContext;
import com.creativeyann17.demo.verticles.HelloConsumer;
import com.creativeyann17.demo.verticles.WebServer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.vertx.core.*;
import io.vertx.ext.cluster.infinispan.ClusterHealthCheck;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App extends AbstractVerticle {

  public static void main(String[] args) {

    Configuration.init();

    log.info("App is starting with Cores: {}", Runtime.getRuntime().availableProcessors());
    log.info("Current profile: {}", Configuration.ENV);
    log.info("UUID: {}", Configuration.UUID);

    var options = new VertxOptions()
      .setMetricsOptions(new MicrometerMetricsOptions()
        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
        .setEnabled(true));

    if (Configuration.Env.prod.equals(Configuration.ENV)) {
      options.setClusterManager(new InfinispanClusterManager());
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
      if (Configuration.Env.prod.equals(Configuration.ENV)) {
        Handler<Promise<Status>> procedure = ClusterHealthCheck.createProcedure(vertx, true);
        healthChecks.register("cluster-health", procedure);
      }

      MeterRegistry registry = BackendRegistries.getDefaultNow();
      SingletonsContext.put(MeterRegistry.class, registry);
      new ClassLoaderMetrics().bindTo(registry);
      new JvmMemoryMetrics().bindTo(registry);
      new JvmGcMetrics().bindTo(registry);
      new ProcessorMetrics().bindTo(registry);
      new JvmThreadMetrics().bindTo(registry);

      return healthChecks;
    });
  }

  private static void startVerticles(Vertx vertx) {
    handleStartingFailure(createHealthChecks(vertx).onSuccess(healthChecks -> {
      SingletonsContext.put(HealthChecks.class, healthChecks);
      handleStartingFailure(
        Future.all(
          vertx.deployVerticle(new HelloConsumer(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)),
          vertx.deployVerticle(new WebServer())).onSuccess((all) -> {
          log.info("App started in {}ms", System.currentTimeMillis() - Configuration.START);
        }));
    }));
  }

  private static void handleStartingFailure(Future<?> verticle) {
    verticle.onFailure(failure -> {
      log.error("", failure);
      System.exit(-1);
    });
  }

}
