package com.creativeyann17.demo.verticles;

import com.creativeyann17.demo.App;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
class WebServerTest {

  final HealthChecks healthChecks = App.putSingleton(HealthChecks.class, Mockito.mock(HealthChecks.class));

  @BeforeEach
  void deploy(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new HelloConsumer());
    vertx.deployVerticle(new WebServer(), testContext.succeedingThenComplete());
  }

  @Test
  void deployed(Vertx vertx, VertxTestContext testContext) {
    testContext.completeNow();
  }

  @Test
  void get_api_v1_hello(Vertx vertx, VertxTestContext testContext) {
    var client = vertx.createHttpClient();
    client.request(HttpMethod.GET, 8080, "localhost", "/api/v1/hello")
      .compose(HttpClientRequest::send)
      .compose(HttpClientResponse::body)
      .onSuccess(buffer -> {
        testContext.verify(() -> {
          assertEquals("Hello World !!!", buffer.toString());
          testContext.completeNow();
        });
      }).onFailure(testContext::failNow);
  }

  @Test
  void get_index(Vertx vertx, VertxTestContext testContext) {
    var client = vertx.createHttpClient();
    client.request(HttpMethod.GET, 8080, "localhost", "/")
      .compose(HttpClientRequest::send)
      .compose(HttpClientResponse::body)
      .onSuccess(buffer -> {
        testContext.verify(() -> {
          assertContent("snapshots/index.html", buffer.toString());
          testContext.completeNow();
        });
      }).onFailure(testContext::failNow);
  }

  private void assertContent(String snapshotPath, String body) throws IOException {
    var expected = IOUtils.resourceToString(snapshotPath, StandardCharsets.UTF_8, getClass().getClassLoader());
    assertEquals(sanitize(expected), sanitize(body));
  }

  private String sanitize(String file) {
    return Arrays.stream(file
        .replaceAll("\t", " ")
        .replaceAll("\\d{4}-\\d{2}-\\d{2}", "0000-00-00") // dates
        .split("\n"))
      .map(String::trim)
      .collect(Collectors.joining("\n"));
  }

}
