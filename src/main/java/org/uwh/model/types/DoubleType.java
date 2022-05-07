package org.uwh.model.types;

import java.io.IOException;
import org.uwh.model.io.DeSer;

public class DoubleType extends Type<Double> {
  DoubleType() {
    super(Double.class);
  }

  @Override
  public <W> void serialize(DeSer<?, W> deser, W w, Double value) throws IOException {
    deser.writeDouble(w, value);
  }

  @Override
  public <R> Double deserialize(DeSer<R, ?> deser, R r) throws IOException {
    return deser.readDouble(r);
  }
}
