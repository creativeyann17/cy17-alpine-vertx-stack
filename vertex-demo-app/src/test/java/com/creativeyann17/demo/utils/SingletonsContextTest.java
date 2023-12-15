package com.creativeyann17.demo.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SingletonsContextTest {

  @Test
  void get_singleton_unknown() {
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      SingletonsContext.get(Object.class);
    });
    assertEquals("Unknown singleton: Object", exception.getMessage());
  }

  @Test
  void put_and_get_singleton() {
    SingletonsContext.put(Integer.class, Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, SingletonsContext.get(Integer.class));
  }

}
