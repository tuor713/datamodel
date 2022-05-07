package org.uwh.model.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.uwh.model.io.DeSer;

public class ListType<T> extends Type<List<T>> {
  private final Type<T> component;

  public ListType(Type<T> component) {
    super((Class) List.class);
    this.component = component;
  }

  @Override
  public <W> void serialize(DeSer<?, W> deser, W w, List<T> list) throws IOException {
    deser.writeUnsigned(w, list.size());
    for (T t : list) {
      component.serialize(deser, w, t);
    }
  }

  @Override
  public <R> List<T> deserialize(DeSer<R, ?> deser, R r) throws IOException {
    long size = deser.readUnsigned(r);
    List<T> res = new ArrayList<>((int) size);
    for (int i=0; i<size; i++) {
      res.add(component.deserialize(deser, r));
    }
    return res;
  }
}
