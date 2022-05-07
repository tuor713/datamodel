package org.uwh.model.types;

import java.io.IOException;
import java.time.Instant;
import org.uwh.model.io.DeSer;


public class TimestampType extends Type<Instant> {
  public TimestampType() {
    super(Instant.class);
  }

  @Override
  public <W> void serialize(DeSer<?, W> deser, W w, Instant value) throws IOException {
    deser.writeLong(w, value.toEpochMilli());
  }

  @Override
  public <R> Instant deserialize(DeSer<R, ?> deser, R r) throws IOException {
    return Instant.ofEpochMilli(deser.readLong(r));
  }
}
