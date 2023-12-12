package com.creativeyann17.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.pebble.PebbleTemplateEngine;

import static com.creativeyann17.demo.App.PORT;
import static com.creativeyann17.demo.App.UUID;

public class WebVerticle extends AbstractVerticle {

  // https://github.com/vert-x3/vertx-web/blob/master/vertx-template-engines/vertx-web-templ-pebble/src/test/java/io/vertx/ext/web/templ/PebbleTemplateTest.java
  // https://github.com/vert-x3/vertx-web/blob/master/vertx-web/src/main/java/examples/WebExamples.java
  // https://vertx.io/docs/vertx-web/java/

  private static final Logger log = LoggerFactory.getLogger(WebVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) {
    var templateEngine = PebbleTemplateEngine.create(vertx, "html");
    var myTemplateHandler = new TemplateHandler(templateEngine);
    var staticHandler = StaticHandler.create("static");
    var gfh = new GlobalFailureHandler();

    var apiRouter = Router.router(vertx);
    apiRouter.get("/hello").handler(this::getHello);
    apiRouter.get("/health").handler(this::getHealth);
    apiRouter.get("/error").handler(this::getError);

    var router = Router.router(vertx);
    router.route("/*").handler(staticHandler);
    //router.route(".*\\.templ").handler(myTemplateHandler);
    router.route("/render").handler(r -> getRender(r, myTemplateHandler)).failureHandler(gfh);
    router.route("/api/*").subRouter(apiRouter);
    //router.errorHandler(404, ErrorHandler.create(vertx));

    var httpServerOptions = new HttpServerOptions()
      .setCompressionSupported(true);

    vertx
      .createHttpServer(httpServerOptions)
      .requestHandler(router)
      .listen(PORT, http -> {
        if (http.succeeded()) {
          startPromise.complete();
          log.info("HTTP server started on port " + PORT);
        } else {
          log.error(http.cause());
          startPromise.fail(http.cause());
        }
      });
  }

  private void getHello(RoutingContext routingContext) {
    vertx.eventBus().request("REQUEST", UUID).onSuccess(handler -> {
      routingContext.response()
        .putHeader("content-type", "text/plain")
        .end(handler.body().toString());
    }).onFailure(failure -> {
      throw new RuntimeException(failure);
    });
  }

  private void getRender(RoutingContext routingContext, TemplateHandler templateHandler) {
    final JsonObject context = new JsonObject()
      .put("value", "foo");
    templateHandler.handle(routingContext, "render2.html", context);
  }

  private void getHealth(RoutingContext routingContext) {
    routingContext.response()
      .putHeader("content-type", "text/plain")
      .end(Json.encodePrettily(STATUS_OK));
  }

  private void getError(RoutingContext routingContext) {
    var code = routingContext.queryParam("code");
    if (code.isEmpty() )
      throw new RuntimeException("foo");
    else if ("404".equals(code.get(0)))
      routingContext.response().setStatusCode(404).end("");
  }

  private record Status(String status){}
  private static final Status STATUS_OK = new Status("OK");
}
