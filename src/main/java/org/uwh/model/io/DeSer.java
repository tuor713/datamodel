package org.uwh.model.io;

import java.io.IOException;


public interface DeSer<R, W> {
  void writeInt(W w, int i) throws IOException;
  int readInt(R r) throws IOException;

  void writeLong(W w, long l) throws IOException;
  long readLong(R r) throws IOException;

  void writeUnsigned(W w, long i) throws IOException;
  long readUnsigned(R r) throws IOException;

  void writeDouble(W w, double d) throws IOException;
  double readDouble(R r) throws IOException;

  void writeFloat(W w, float f) throws IOException;
  float readFloat(R r) throws IOException;

  void writeString(W w, String s) throws IOException;
  String readString(R r) throws IOException;

  void writeBytes(W w, byte[] bytes) throws IOException;
  byte[] readBytes(R r) throws IOException;
}
