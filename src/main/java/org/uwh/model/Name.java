package org.uwh.model;

import java.util.Objects;


/**
 * Name are case-insensitive representation of business terms
 */
public class Name {
  private final String namespace;
  private final String name;
  private final int myHash;

  public static Name of(String ns, String name) {
    return new Name(ns, name);
  }

  public static Name ofQualified(String qualified) {
    String[] parts = qualified.split("/");
    return Name.of(parts[0], parts[1]);
  }

  public Name(String ns, String name) {
    this.namespace = ns;
    this.name = name;
    myHash = Objects.hash(namespace.toLowerCase(), name.toLowerCase());
  }

  public String getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Name name1 = (Name) o;
    return Objects.equals(namespace.toLowerCase(), name1.namespace.toLowerCase())
        && Objects.equals(name.toLowerCase(), name1.name.toLowerCase());
  }

  @Override
  public int hashCode() {
    return myHash;
  }

  @Override
  public String toString() {
    return namespace + '/' + name;
  }
}
