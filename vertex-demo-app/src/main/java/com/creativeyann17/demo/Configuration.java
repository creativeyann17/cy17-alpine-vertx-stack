package com.creativeyann17.demo;

import ch.qos.logback.classic.Level;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Slf4j
public class Configuration {

  public final static String UUID = java.util.UUID.randomUUID().toString();

  private static final String ENV = System.getenv("ENV");
  private static final String PORT = System.getenv("PORT");

  public static void init() {
    System.setProperty("vertxweb.environment", env().name());
    System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
    var root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    root.setLevel(Env.prod.equals(env()) ? Level.ERROR : Level.INFO);
  }

  public static Env env() {
    return Optional.ofNullable(ENV).filter(StringUtils::isNotBlank).map(Env::valueOf).orElse(Env.prod);
  }

  public static boolean isProd() {
    return Env.prod.equals(env());
  }

  public static int port() {
    return Integer.parseInt(Optional.ofNullable(PORT).orElse("8080"));
  }

  public enum Env {
    dev, prod
  }
}
