package org.uwh.model.io;

import java.io.IOException;


public interface DeSer<R, W> {
  void writeInt(W w, int i) throws IOException;
  int readInt(R r) throws IOException;

  void writeUnsigned(W w, long i) throws IOException;
  long readUnsigned(R r) throws IOException;

  void writeDouble(W w, double d) throws IOException;
  double readDouble(R r) throws IOException;

  void writeString(W w, String s) throws IOException;
  String readString(R r) throws IOException;
}
