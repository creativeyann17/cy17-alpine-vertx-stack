package com.creativeyann17.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationTest {

  @Test
  void env() {
    assertEquals(Configuration.Env.prod, Configuration.env());
  }

  @Test
  void isProd() {
    assertTrue(Configuration.isProd());
  }

  @Test
  void port() {
    assertEquals(8080, Configuration.port());
  }
}
