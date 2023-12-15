package com.creativeyann17.demo.verticles;

import com.creativeyann17.demo.handlers.TemplateHandler;
import com.creativeyann17.demo.utils.Configuration;
import com.creativeyann17.demo.utils.SingletonsContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.pebble.PebbleTemplateEngine;
import io.vertx.micrometer.PrometheusScrapingHandler;
import io.vertx.micrometer.backends.BackendRegistries;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.creativeyann17.demo.verticles.HelloConsumer.HELLO_EVENT;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
public class WebServer extends AbstractVerticle {

  // https://github.com/vert-x3/vertx-web/blob/master/vertx-template-engines/vertx-web-templ-pebble/src/test/java/io/vertx/ext/web/templ/PebbleTemplateTest.java
  // https://github.com/vert-x3/vertx-web/blob/master/vertx-web/src/main/java/examples/WebExamples.java
  // https://vertx.io/docs/vertx-web/java/
  // https://github.com/aesteve/vertx-feeds/blob/master/src/main/java/io/vertx/examples/feeds/verticles/WebServer.java

  //private static final Logger log = LoggerFactory.getLogger(WebVerticle.class);

  private static final String APPLICATION_JSON = HttpHeaderValues.APPLICATION_JSON.toString();
  private static final String PUBLIC_PATH = "public";

  private StaticHandler staticHandler;
  private TemplateEngine templateEngine;
  private TemplateHandler templateHandler;
  private FaviconHandler faviconHandler;
  private HealthCheckHandler healthHandler;
  private HealthChecks healthChecks;
  private Handler<RoutingContext> prometheusScrapingHandler;
  //private Timer timerIndex;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    this.staticHandler = StaticHandler.create(PUBLIC_PATH + "/static");
    this.templateEngine = PebbleTemplateEngine.create(vertx, "html");
    this.templateHandler = new TemplateHandler(templateEngine, PUBLIC_PATH + "/templates");
    this.faviconHandler = FaviconHandler.create(vertx, PUBLIC_PATH + "/static/images/favicon.ico");
    this.healthChecks = SingletonsContext.get(HealthChecks.class);
    this.healthHandler = HealthCheckHandler.createWithHealthChecks(healthChecks);
    this.prometheusScrapingHandler = PrometheusScrapingHandler.create();

    /*timerIndex = Timer
      .builder("index timer")
      .description("how much time takes index to compute")
      .register(SingletonsContext.get(MeterRegistry.class));*/
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx
      .createHttpServer(createOptions())
      .requestHandler(createRouter())
      .listen(result -> {
        if (result.succeeded()) {
          startPromise.complete();
          healthChecks.register("web", promise -> promise.complete(Status.OK()));
          log.info("HTTP server started on port: " + Configuration.PORT);
        } else {
          healthChecks.register("web", promise -> promise.complete(Status.KO()));
          startPromise.fail(result.cause());
        }
      });
  }

  private HttpServerOptions createOptions() {
    return new HttpServerOptions()
      .setCompressionSupported(false)
      .setPort(Configuration.PORT);
  }

  private Router createRouter() {
    var router = Router.router(vertx);
    router.route().failureHandler(this::handleGlobalFailure);
    router.route().handler(faviconHandler);
    router.route("/").handler(this::handleIndex);
    router.route("/public/static/*").handler(staticHandler);
    router.route("/actuator/*").subRouter(createActuators());
    router.route("/api/v1/*").subRouter(createAPIv1());
    //router.route().handler(this::handleRedirectToIndex);
    router.route().handler(this::handleNotFound);
    return router;
  }

  private void handleHealth(RoutingContext routingContext) {
    handleAuth(routingContext, false, (auth) -> {
      if (auth == null) {
        routingContext.response().end(JsonObject.of("status", "UP").encodePrettily());
      } else {
        healthHandler.handle(routingContext);
      }
    });
  }

  private void handleStatus(RoutingContext routingContext) {
    handleAuth(routingContext, true, (auth) -> {
      MeterRegistry registry = BackendRegistries.getDefaultNow();
      StringBuilder builder = new StringBuilder();
      registry.forEachMeter(m -> {
        builder.append(m.getId().getName() + " ");
        m.measure().forEach(mes -> {
          builder.append(mes.getValue() + " ");
        });
        builder.append("\n");
      });
      routingContext.response().end(builder.toString());
    });
  }

  private void handleMetrics(RoutingContext routingContext) {
    handleAuth(routingContext, true, (auth) -> {
      prometheusScrapingHandler.handle(routingContext);
    });
  }

  private void handleAuth(RoutingContext routingContext, boolean strict, Handler<String> handler) {
    var auth = routingContext.request().getHeader(AUTHORIZATION);
    if (StringUtils.isBlank(Configuration.X_API_KEY) || !Configuration.X_API_KEY.equals(StringUtils.replace(auth, "Bearer ", ""))) {
      if (strict) {
        routingContext.response().setStatusCode(403).end();
      } else {
        handler.handle(null);
      }
    } else {
      handler.handle(auth);
    }
  }

  private Router createActuators() {
    var router = Router.router(vertx);
    router.route().produces(APPLICATION_JSON);
    router.route().handler(context -> {
      context.response().headers().add(CONTENT_TYPE, APPLICATION_JSON);
      context.next();
    });
    router.route("/health").handler(this::handleHealth);
    router.route("/metrics").handler(this::handleMetrics);
    router.route("/status").handler(this::handleStatus);
    return router;
  }

  private Router createAPIv1() {
    var router = Router.router(vertx);
    router.route().consumes(APPLICATION_JSON);
    router.route().produces(APPLICATION_JSON);
    router.route().handler(BodyHandler.create());
    router.route().handler(context -> {
      context.response().headers().add(CONTENT_TYPE, APPLICATION_JSON);
      context.next();
    });
    router.route("/hello").handler(this::handleHello);
    return router;
  }

  private void handleGlobalFailure(RoutingContext routingContext) {
    var exception = routingContext.failure();
    log.error("", exception);
    routingContext.response().setStatusCode(500).end();
  }

  private void handleRedirectToIndex(RoutingContext rc) {
    log.warn("Redirect: {} => {}", getRemoteAddr(rc), rc.request().absoluteURI());
    HttpServerResponse response = rc.response();
    response.setStatusCode(303);
    response.headers().add("Location", "/");
    response.end();
  }

  private void handleNotFound(RoutingContext rc) {
    log.warn("Not found: {} => {}", getRemoteAddr(rc), rc.request().absoluteURI());
    log.warn("Details:\n{}\n{}", rc.request().headers(), rc.request().body().result());
    rc.response().setStatusCode(404).end();
  }

  private void handleIndex(RoutingContext routingContext) {
    //timerIndex.record(() ->{
    final JsonObject context = new JsonObject()
      .put("value", "foo");
    templateHandler.handle(routingContext, "index.html", context);
    // });
  }

  private void handleHello(RoutingContext routingContext) {
    vertx.eventBus().request(HELLO_EVENT, Configuration.UUID).onSuccess(handler -> {
      routingContext.response()
        .putHeader(CONTENT_TYPE, TEXT_PLAIN)
        .end(String.valueOf(handler.body()));
    }).onFailure(routingContext::fail);
  }

  private String getRemoteAddr(RoutingContext routingContext) {
    return Optional.ofNullable(routingContext.request().getHeader("X-Real-IP")).orElse(routingContext.request().remoteAddress().toString());
  }

}
