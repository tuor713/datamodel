package org.uwh.model.types;

import java.io.IOException;
import org.uwh.model.io.DeSer;

public class IntType extends Type<Integer> {
  IntType() {
    super(Integer.class);
  }

  @Override
  public <W> void serialize(DeSer<?, W> deser, W w, Integer value) throws IOException {
    deser.writeInt(w, value);
  }

  @Override
  public <R> Integer deserialize(DeSer<R,?> deser, R r) throws IOException {
    return deser.readInt(r);
  }
}
