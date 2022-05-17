package org.uwh.model.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.uwh.model.Context;
import org.uwh.model.Record;
import org.uwh.model.Schema;
import org.uwh.model.Term;
import org.uwh.model.Vocabulary;


public class DeSerUtil {
  public static byte[] serialize(Record rec) throws IOException {
    Map<Integer, Object> values = rec.getValues();
    Vocabulary vocab = rec.getContext().getVocab();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(bos);
    DeSer<DataInputStream, DataOutputStream> deser = new BinaryDeSer();

    deser.writeUnsigned(dos, values.size());

    for (Map.Entry<Integer,Object> e : values.entrySet()) {
      deser.writeUnsigned(dos, e.getKey());
      Term t = vocab.getTerm(e.getKey()).orElseThrow();
      t.getType().serialize(deser, dos, e.getValue());
    }
    dos.flush();

    return bos.toByteArray();
  }

  public static Record deserialize(Context ctx, Schema schema, byte[] bytes) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    DataInputStream dis = new DataInputStream(bis);
    DeSer<DataInputStream, DataOutputStream> deser = new BinaryDeSer();
    Vocabulary vocab = ctx.getVocab();

    Map<Integer,Object> values = new HashMap<>();
    long noFields = deser.readUnsigned(dis);

    for (int i=0; i<noFields; i++) {
      int tag = (int) deser.readUnsigned(dis);
      Term t = vocab.getTerm(tag).orElseThrow(() -> new IllegalStateException("No term defined for tag " + tag));
      values.put(tag, t.getType().deserialize(deser, dis));
    }

    return new Record(ctx, schema, values);
  }
}
