package org.uwh.model.types;

import java.io.IOException;
import org.uwh.model.io.DeSer;


public class FloatType extends Type<Float> {
  public FloatType() {
    super(Float.class);
  }

  @Override
  public <W> void serialize(DeSer<?, W> deser, W w, Float value) throws IOException {
    deser.writeFloat(w, value);
  }

  @Override
  public <R> Float deserialize(DeSer<R, ?> deser, R r) throws IOException {
    return deser.readFloat(r);
  }
}
