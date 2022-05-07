package org.uwh.model.io;

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
  public void writeLong(DataOutputStream os, long l) throws IOException {
    os.writeLong(l);
  }

  @Override
  public long readLong(DataInputStream is) throws IOException {
    return is.readLong();
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
  public void writeFloat(DataOutputStream os, float f) throws IOException {
    os.writeFloat(f);
  }

  @Override
  public float readFloat(DataInputStream is) throws IOException {
    return is.readFloat();
  }

  @Override
  public void writeString(DataOutputStream os, String s) throws IOException {
    os.writeUTF(s);
  }

  @Override
  public String readString(DataInputStream is) throws IOException {
    return is.readUTF();
  }

  @Override
  public void writeBytes(DataOutputStream os, byte[] bytes) throws IOException {
    writeUnsigned(os, bytes.length);
    os.write(bytes);
  }

  @Override
  public byte[] readBytes(DataInputStream is) throws IOException {
    long len = readUnsigned(is);
    return is.readNBytes((int) len);
  }
}
