package org.uwh.model.types;

import java.io.IOException;
import java.time.LocalDate;
import org.uwh.model.io.DeSer;


public class DateType extends Type<LocalDate> {
  public DateType() {
    super(LocalDate.class);
  }

  @Override
  public <W> void serialize(DeSer<?, W> deser, W w, LocalDate value) throws IOException {
    deser.writeLong(w, value.toEpochDay());
  }

  @Override
  public <R> LocalDate deserialize(DeSer<R, ?> deser, R r) throws IOException {
    return LocalDate.ofEpochDay(deser.readLong(r));
  }
}
