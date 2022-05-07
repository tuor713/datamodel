package org.uwh.model.types;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.uwh.model.io.DeSer;


public class MapType<K,V> extends Type<Map<K,V>> {
  private final Type<K> keyType;
  private final Type<V> valueType;

  public MapType(Type<K> keyType, Type<V> valueType) {
    super((Class) Map.class);
    this.keyType = keyType;
    this.valueType = valueType;
  }

  @Override
  public <W> void serialize(DeSer<?, W> deser, W w, Map<K, V> value) throws IOException {
    deser.writeUnsigned(w, value.values().stream().filter(Objects::nonNull).count());
    for (Map.Entry<K,V> e : value.entrySet()) {
      if (e.getValue() != null) {
        keyType.serialize(deser, w, e.getKey());
        valueType.serialize(deser, w, e.getValue());
      }
    }
  }

  @Override
  public <R> Map<K, V> deserialize(DeSer<R, ?> deser, R r) throws IOException {
    long len = deser.readUnsigned(r);
    Map<K,V> res = new HashMap<>();
    for (int i=0; i<len; i++) {
      K key = keyType.deserialize(deser, r);
      V value = valueType.deserialize(deser, r);
      res.put(key, value);
    }

    return res;
  }
}
