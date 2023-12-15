package com.creativeyann17.demo.utils;

import java.util.HashMap;
import java.util.Map;

public class SingletonsContext {

  private static final Map<String, Object> singletons = new HashMap<>();

  public synchronized static <T> T put(Class<?> c, T o) {
    return (T) singletons.put(c.getSimpleName(), o);
  }

  public synchronized static <T> T get(Class<T> c) {
    String name = c.getSimpleName();
    if (!singletons.containsKey(name))
      throw new RuntimeException("Unknown singleton: " + name);
    return (T) singletons.get(name);
  }

}
