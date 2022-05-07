package org.uwh.model.types;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.uwh.model.io.DeSer;

public abstract class Type<T> {
  private final Class<T> clazz;

  public static final Type<Integer> INT = new IntType();
  public static final Type<Long> LONG = new LongType();
  public static final Type<Double> DOUBLE = new DoubleType();
  public static final Type<Float> FLOAT = new FloatType();
  public static final Type<String> STRING = new StringType();
  public static final Type<LocalDate> DATE = new DateType();
  public static final Type<Instant> TIMESTAMP = new TimestampType();
  public static final Type BYTES = new BytesType();

  private static final Map<String,Type> typeMap = Map.of(
      "int", INT,
      "long", LONG,
      "double", DOUBLE,
      "float", FLOAT,
      "string", STRING,
      "date", DATE,
      "timestamp", TIMESTAMP,
      "bytes", BYTES);

  public static Type<?> forString(String type) {
    Type<?> res = typeMap.get(type);
    if (res == null) {
      throw new IllegalArgumentException("Unknown type " + type);
    }
    return res;
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
