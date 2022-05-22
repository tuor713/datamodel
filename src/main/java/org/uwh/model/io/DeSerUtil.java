package org.uwh.model.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.uwh.model.Context;
import org.uwh.model.Record;
import org.uwh.model.Schema;
import org.uwh.model.Term;


public class DeSerUtil {
  private static Map<Term<?>, Integer> createTagAssignment(Context ctx) {
    var vocab = ctx.getVocab();
    var sortedVocab = new ArrayList<>(vocab.getTerms());
    sortedVocab.sort(Comparator.comparing(t -> t.getName().getQualifiedName()));

    var result = new HashMap<Term<?>, Integer>();
    int i = 0;
    for (var t : sortedVocab) {
      result.put(t, i);
      i++;
    }
    return result;
  }

  private static<U,V> Map<U,V> reverseMap(Map<V,U> map) {
    var result = new HashMap<U,V>();
    for (Map.Entry<V,U> e : map.entrySet()) {
      result.put(e.getValue(), e.getKey());
    }
    return result;
  }

  public static byte[] serialize(Record rec) throws IOException {
    Map<Term<?>, Object> values = rec.getValues();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(bos);
    DeSer<DataInputStream, DataOutputStream> deser = new BinaryDeSer();
    Map<Term<?>, Integer> term2tag = createTagAssignment(rec.getContext());

    deser.writeUnsigned(dos, values.size());

    for (Map.Entry<Term<?>,Object> e : values.entrySet()) {
      Term t = e.getKey();
      deser.writeUnsigned(dos, term2tag.get(t));
      t.getType().serialize(deser, dos, e.getValue());
    }
    dos.flush();

    return bos.toByteArray();
  }

  public static Record deserialize(Context ctx, Schema schema, byte[] bytes) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    DataInputStream dis = new DataInputStream(bis);
    DeSer<DataInputStream, DataOutputStream> deser = new BinaryDeSer();
    Map<Integer,Term<?>> tag2term = reverseMap(createTagAssignment(ctx));

    Map<Term<?>,Object> values = new HashMap<>();
    long noFields = deser.readUnsigned(dis);

    for (int i=0; i<noFields; i++) {
      int tag = (int) deser.readUnsigned(dis);
      Term t = tag2term.get(tag);
      if (t == null) {
        throw new IllegalStateException("No term defined for tag " + tag);
      }
      values.put(t, t.getType().deserialize(deser, dis));
    }

    return new Record(ctx, schema, values);
  }
}
