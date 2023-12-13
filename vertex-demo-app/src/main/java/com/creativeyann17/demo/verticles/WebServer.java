package com.creativeyann17.demo.verticles;

import com.creativeyann17.demo.Configuration;
import com.creativeyann17.demo.handlers.*;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.pebble.PebbleTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import static com.creativeyann17.demo.verticles.HelloConsumer.HELLO_EVENT;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
public class WebServer extends AbstractVerticle {

  // https://github.com/vert-x3/vertx-web/blob/master/vertx-template-engines/vertx-web-templ-pebble/src/test/java/io/vertx/ext/web/templ/PebbleTemplateTest.java
  // https://github.com/vert-x3/vertx-web/blob/master/vertx-web/src/main/java/examples/WebExamples.java
  // https://vertx.io/docs/vertx-web/java/
  // https://github.com/aesteve/vertx-feeds/blob/master/src/main/java/io/vertx/examples/feeds/verticles/WebServer.java

  //private static final Logger log = LoggerFactory.getLogger(WebVerticle.class);

  private final String APPLICATION_JSON = HttpHeaderValues.APPLICATION_JSON.toString();

  private GlobalFailureHandler globalFailureHandler;
  private StaticHandler staticHandler;
  private TemplateEngine templateEngine;
  private TemplateHandler templateHandler;
  private HealthHandler healthHandler;
  private RedirectHandler redirectHandler;
  private FaviconHandler faviconHandler;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    this.globalFailureHandler = new GlobalFailureHandler();
    this.staticHandler = StaticHandler.create("public/static");
    this.healthHandler = new HealthHandler();
    this.templateEngine = PebbleTemplateEngine.create(vertx, "html");
    this.templateHandler = new TemplateHandler(vertx, templateEngine, "public/templates");
    this.redirectHandler = new RedirectHandler();
    this.faviconHandler = FaviconHandler.create(vertx, "public/static/images/favicon.ico");
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx
      .createHttpServer(createOptions())
      .requestHandler(createRouter())
      .listen(result -> {
        if (result.succeeded()) {
          startPromise.complete();
          log.info("HTTP server started on port: " + Configuration.port());
        } else {
          startPromise.fail(result.cause());
        }
      });
  }

  private HttpServerOptions createOptions() {
    return new HttpServerOptions()
      .setCompressionSupported(true)
      .setCompressionLevel(HttpServerOptions.DEFAULT_COMPRESSION_LEVEL)
      .setPort(Configuration.port());
  }

  private Router createRouter() {
    var router = Router.router(vertx);
    router.route().failureHandler(globalFailureHandler);
    router.route().handler(faviconHandler);
    router.route("/").handler(this::handleIndex);
    router.route("/public/static/*").handler(staticHandler);
    router.route("/actuator/*").subRouter(createActuators());
    router.route("/api/v1/*").subRouter(createAPIv1());
    router.route().handler(redirectHandler);
    return router;
  }

  private Router createActuators() {
    var router = Router.router(vertx);
    router.route().produces(APPLICATION_JSON);
    router.route().handler(context -> {
      context.response().headers().add(CONTENT_TYPE, APPLICATION_JSON);
      context.next();
    });
    router.route("/health").handler(healthHandler);
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

  private void handleIndex(RoutingContext routingContext) {
    final JsonObject context = new JsonObject()
      .put("value", "foo");
    templateHandler.handle(routingContext, "index.html", context);
  }

  private void handleHello(RoutingContext routingContext) {
    vertx.eventBus().request(HELLO_EVENT, Configuration.UUID).onSuccess(handler -> {
      routingContext.response()
        .putHeader(CONTENT_TYPE, TEXT_PLAIN)
        .end(String.valueOf(handler.body()));
    }).onFailure(failure -> {
      throw new RuntimeException(failure);
    });
  }

}
