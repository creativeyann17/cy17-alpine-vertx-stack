package com.creativeyann17.demo.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationTest {

  @Test
  void default_env() {
    assertEquals(Configuration.Env.prod, Configuration.ENV);
  }

  @Test
  void default_port() {
    assertEquals(8080, Configuration.PORT);
  }
}
