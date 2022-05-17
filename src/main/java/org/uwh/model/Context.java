package org.uwh.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    List<Namespace> sortedNamespaces = new ArrayList<>(namespaces);
    sortedNamespaces.sort(Comparator.comparing(Namespace::getName));
    final int multiply = sortedNamespaces.size();
    for (int i=0; i<sortedNamespaces.size(); i++) {
      final int idx = i;
      sortedNamespaces.set(i, sortedNamespaces.get(i).withTagTranslation(id -> id*multiply+idx));
    }

    this.namespaces = new HashSet<>(sortedNamespaces);
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
