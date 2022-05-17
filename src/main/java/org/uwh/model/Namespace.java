package org.uwh.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;


public class Namespace implements TagTranslation<Namespace> {
  private final String name;
  private final SemVer version;
  private final Vocabulary vocab;
  private final Map<Name,Schema> schemas;

  public Namespace(String name, SemVer version, Vocabulary vocab, Map<Name,Schema> schemas) {
    this.name = name;
    this.version = version;
    this.vocab = vocab;
    this.schemas = new HashMap<>(schemas);
  }

  public String getName() {
    return name;
  }

  public SemVer getVersion() {
    return version;
  }

  public Context toContext() {
    return new Context(this);
  }

  public Vocabulary getVocab() {
    return vocab;
  }

  public Map<Name, Schema> getSchemas() {
    return schemas;
  }

  @Override
  public Namespace withTagTranslation(Function<Integer, Integer> mapper) {
    Map<Name,Schema> newSchemas = new HashMap<>();
    schemas.values().forEach(s -> {
      Schema newSchema = s.withTagTranslation(mapper);
      newSchemas.put(newSchema.getName(), newSchema);
    });

    return new Namespace(name, version, vocab.withTagTranslation(mapper), newSchemas);
  }

  @Override
  public String toString() {
    return "Namespace{" +
        "name='" + name + '\'' +
        ", version=" + version +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Namespace namespace = (Namespace) o;
    return Objects.equals(name, namespace.name) && Objects.equals(version, namespace.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, version);
  }
}
