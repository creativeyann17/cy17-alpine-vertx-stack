package com.creativeyann17.demo.utils;

import ch.qos.logback.classic.Level;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Slf4j
public class Configuration {

  public static final long START = System.currentTimeMillis();
  public static final String UUID = java.util.UUID.randomUUID().toString().replace("-", "");

  public static final Env ENV = Env.valueOf(getEnvOrDefault("ENV", "prod"));
  public static final int PORT = Integer.parseInt(getEnvOrDefault("PORT", "8080"));
  public static final String X_API_KEY = getEnvOrDefault("X_API_KEY", UUID);

  public static void init() {
    System.setProperty("vertxweb.environment", ENV.name());
    System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
    var root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    root.setLevel(Env.prod.equals(ENV) ? Level.ERROR : Level.INFO);
  }

  private static String getEnvOrDefault(String name, String defaultValue) {
    return Optional.ofNullable(System.getenv(name)).filter(StringUtils::isNotBlank).orElse(defaultValue);
  }

  public enum Env {
    dev, prod
  }

}
