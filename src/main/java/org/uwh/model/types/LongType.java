package org.uwh.model.types;

import java.io.IOException;
import org.uwh.model.io.DeSer;


public class LongType extends Type<Long> {
  public LongType() {
    super(Long.class);
  }

  @Override
  public <W> void serialize(DeSer<?, W> deser, W w, Long value) throws IOException {
    deser.writeLong(w, value);
  }

  @Override
  public <R> Long deserialize(DeSer<R, ?> deser, R r) throws IOException {
    return deser.readLong(r);
  }
}
