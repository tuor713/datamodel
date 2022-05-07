package org.uwh.model.types;

import java.io.IOException;
import org.uwh.model.io.DeSer;

public abstract class Type<T> {
  private final Class<T> clazz;

  public static final Type<Integer> INT = new IntType();
  public static final Type<Double> DOUBLE = new DoubleType();
  public static final Type<String> STRING = new StringType();

  public static Type<?> forString(String type) {
    if ("int".equals(type)) {
      return INT;
    } else if ("string".equals(type)) {
      return STRING;
    } else if ("double".equals(type)) {
      return DOUBLE;
    } else {
      throw new IllegalArgumentException("Unknown type " + type);
    }
  }

  public Type(Class<T> clazz) {
    this.clazz = clazz;
  }

  public Class<T> getClazz() {
    return clazz;
  }

  public abstract<W> void serialize(DeSer<?,W> deser, W w, T value) throws IOException;
  public abstract<R> T deserialize(DeSer<R,?> deser, R r) throws IOException;
}
