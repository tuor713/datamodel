package org.uwh.model.types;

import java.io.IOException;
import org.uwh.model.io.DeSer;

public class StringType extends Type<String> {
  StringType() {
    super(String.class);
  }

  @Override
  public <W> void serialize(DeSer<?, W> deser, W w, String value) throws IOException {
    deser.writeString(w, value);
  }

  @Override
  public <R> String deserialize(DeSer<R,?> deser, R r) throws IOException {
    return deser.readString(r);
  }
}
