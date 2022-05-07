package org.uwh.model.types;

import java.io.IOException;
import org.uwh.model.io.DeSer;


public class BytesType extends Type {
  public BytesType() {
    super(byte[].class);
  }

  @Override
  public void serialize(DeSer deser, Object w, Object bytes) throws IOException {
    deser.writeBytes(w, (byte[]) bytes);
  }

  @Override
  public Object deserialize(DeSer deser, Object r) throws IOException {
    return deser.readBytes(r);
  }
}
