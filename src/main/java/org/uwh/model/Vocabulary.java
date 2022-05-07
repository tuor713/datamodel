package org.uwh.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Vocabulary {
  private final Map<Integer, Term<?>> vocab;

  public Vocabulary() {
    vocab = new HashMap<>();
  }

  public Vocabulary(Collection<Term<?>> vocab) {
    this.vocab = new HashMap<>();
    vocab.forEach(this::insertTerm);
  }

  public void insertTerm(Term<?> t) {
    if (vocab.containsKey(t.getTag())) {
      throw new IllegalArgumentException("Tag " + t.getTag() + " is already defined");
    }

    if (lookupTerm(t.getName()).isPresent()) {
      throw new IllegalArgumentException("Name " + t.getName() + " is already defined");
    }
    Optional<Name> dupeAlias = t.getAliases().stream().filter(n -> lookupTerm(n).isPresent()).findAny();
    if (dupeAlias.isPresent()) {
      throw new IllegalArgumentException("Alias " + dupeAlias.get() + " is already defined");
    }

    vocab.put(t.getTag(), t);
  }

  public boolean isValidTerm(Term<?> t) {
    Term<?> existing = vocab.get(t.getTag());

    // we check for *exact* match, users must get Term objects via the lookup function
    return t == existing;
  }

  public Optional<Term<?>> getTerm(int tag) {
    return Optional.ofNullable(vocab.get(tag));
  }

  public Optional<Term<?>> lookupTerm(Name name) {
    // TODO more efficient implementation
    return vocab.values().stream().filter(t -> t.getName().equals(name) || t.getAliases().stream().anyMatch(n -> n.equals(name))).findAny();
  }

  public int size() {
    return vocab.size();
  }
}
