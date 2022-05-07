package org.uwh.model.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BinaryDeSer implements DeSer<DataInputStream, DataOutputStream> {
  @Override
  public void writeInt(DataOutputStream os, int i) throws IOException {
    int zigzag = (i << 1) ^ (i >> 31);
    writeUnsigned(os, zigzag);
  }

  @Override
  public int readInt(DataInputStream is) throws IOException {
    long zigzag = readUnsigned(is);
    return (int) ((zigzag >>> 1) ^ -(zigzag & 1));
  }

  @Override
  public void writeLong(DataOutputStream os, long l) throws IOException {
    long zigzag = (l << 1) ^ (l >> 63);
    writeUnsigned(os, zigzag);
  }

  @Override
  public long readLong(DataInputStream is) throws IOException {
    long zigzag = readUnsigned(is);
    return (zigzag >>> 1) ^ -(zigzag & 1);
  }

  @Override
  public void writeUnsigned(DataOutputStream os, long i) throws IOException {
    long mask = 127;
    do {
      long lowest = i & mask;
      i = i >> 7;
      if (i != 0) {
        lowest = lowest | 128;
      }
      os.write((int) lowest);
    } while (i != 0);
  }

  @Override
  public long readUnsigned(DataInputStream is) throws IOException {
    int b;
    long res = 0;
    int shift = 0;
    do {
      b = is.read();
      long add = (((long) b) & 127) << shift;
      res += add;
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
