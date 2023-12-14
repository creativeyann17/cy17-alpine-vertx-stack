package com.creativeyann17.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppSingletonsTest {

  @Test
  void get_singleton_unknown() {
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      App.getSingleton(Object.class);
    });
    assertEquals("Unknown singleton: Object", exception.getMessage());
  }

  @Test
  void put_and_get_singleton() {
    App.putSingleton(Integer.class, Integer.MAX_VALUE);
    var value = App.getSingleton(Integer.class);
    assertEquals(Integer.MAX_VALUE, value);
  }

}
