package org.uwh.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Represents a context for serialization, deserialization comprised
 * of multiple versioned namespaces.
 */
public class Context {
  private final Set<Namespace> namespaces;
  private final Vocabulary vocab;
  private final Map<Name,Schema> schemas;

  public Context(Namespace singleNs) {
    namespaces = Collections.singleton(singleNs);
    vocab = singleNs.getVocab();
    schemas = singleNs.getSchemas();
  }

  public Context(Set<Namespace> namespaces) {
    this.namespaces = new HashSet<>(namespaces);
    this.vocab = Vocabulary.createJointVocab(this.namespaces.stream().map(Namespace::getVocab).collect(Collectors.toList()));
    this.schemas = new HashMap<>();
    this.namespaces.forEach(n -> this.schemas.putAll(n.getSchemas()));
  }

  public Set<Namespace> getNamespaces() {
    return namespaces;
  }

  public Vocabulary getVocab() {
    return vocab;
  }

  public Map<Name, Schema> getSchemas() {
    return schemas;
  }

  public Optional<Schema> getSchema(Name name) {
    return Optional.ofNullable(schemas.get(name));
  }

  public Optional<Schema> getSchema(String qName) {
    return getSchema(Name.ofQualified(qName));
  }

  @Override
  public String toString() {
    return "Context{" +
        "namespaces=" + namespaces +
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
    Context context = (Context) o;
    return Objects.equals(namespaces, context.namespaces);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespaces);
  }
}
