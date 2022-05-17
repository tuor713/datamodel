package org.uwh.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public class Vocabulary implements TagTranslation<Vocabulary> {
  private final Map<Integer, Term<?>> vocab;

  public Vocabulary() {
    vocab = new HashMap<>();
  }

  public Vocabulary(Collection<Term<?>> vocab) {
    this.vocab = new HashMap<>();
    vocab.forEach(this::insertTerm);
  }

  public static Vocabulary createJointVocab(List<Vocabulary> vocabs) {
    Vocabulary result = new Vocabulary();
    vocabs.forEach(v -> v.getTerms().forEach(result::insertTerm));
    return result;
  }

  @Override
  public Vocabulary withTagTranslation(Function<Integer, Integer> mapper) {
    return new Vocabulary(vocab.values().stream().map(t -> t.withTagTranslation(mapper)).collect(Collectors.toList()));
  }

  public void insertTerm(Term<?> t) {
    if (vocab.containsKey(t.getTag())) {
      throw new IllegalArgumentException("Tag " + t.getTag() + " is already defined");
    }

    if (hasTerm(t.getName())) {
      throw new IllegalArgumentException("Name " + t.getName() + " is already defined");
    }
    Optional<Name> dupeAlias = t.getAliases().stream().filter(n -> hasTerm(n)).findAny();
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

  private Collection<Term<?>> getTerms() {
    return vocab.values();
  }

  public boolean hasTerm(Name name) {
    return lookupTerm(name).isPresent();
  }

  public int size() {
    return vocab.size();
  }
}
