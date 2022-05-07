package org.uwh.model.types;

import java.io.IOException;
import java.util.List;
import org.uwh.model.io.DeSer;

public class UnionType extends Type {
  private final Type[] types;

  public UnionType(Type[] types) {
    super(Object.class);
    this.types = types;
  }

  @Override
  public void serialize(DeSer deser, Object w, Object value) throws IOException {
    int idx = -1;
    for (int i=0; i<types.length; i++) {
      if (types[i].getClazz().isInstance(value)) {
        idx = i;
        break;
      }
    }
    if (idx == -1) {
      throw new IllegalStateException("Value " + value + " does not match any of the types in union " + List.of(types));
    }

    deser.writeUnsigned(w, idx);
    types[idx].serialize(deser, w, value);
  }

  @Override
  public Object deserialize(DeSer deser, Object r) throws IOException {
    int idx = (int) deser.readUnsigned(r);
    return types[idx].deserialize(deser, r);
  }
}
