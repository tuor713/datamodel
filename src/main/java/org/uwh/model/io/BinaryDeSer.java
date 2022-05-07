package org.uwh.model.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BinaryDeSer implements DeSer<DataInputStream, DataOutputStream> {
  @Override
  public void writeInt(DataOutputStream os, int i) throws IOException {
    os.writeInt(i);
  }

  @Override
  public int readInt(DataInputStream is) throws IOException {
    return is.readInt();
  }

  @Override
  public void writeUnsigned(DataOutputStream os, long i) throws IOException {
    long mask = 127;
    do {
      long lowest = i & mask;
      os.write((int) lowest);
      i = i >> 7;
    } while (i != 0);
  }

  @Override
  public long readUnsigned(DataInputStream is) throws IOException {
    int b;
    long res = 0;
    int shift = 0;
    do {
      b = is.read();
      res += b << shift;
      shift += 7;
    } while ((b & 128) != 0);
    return res;
  }

  @Override
  public void writeDouble(DataOutputStream os, double d) throws IOException {
    os.writeDouble(d);
  }

  @Override
  public double readDouble(DataInputStream is) throws IOException {
    return is.readDouble();
  }

  @Override
  public void writeString(DataOutputStream os, String s) throws IOException {
    os.writeUTF(s);
  }

  @Override
  public String readString(DataInputStream is) throws IOException {
    return is.readUTF();
  }
}
