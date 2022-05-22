package org.uwh.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class Vocabulary {
  private final Set<Term<?>> vocab;

  public Vocabulary() {
    vocab = new HashSet<>();
  }

  public Vocabulary(Collection<Term<?>> vocab) {
    this.vocab = new HashSet<>();
    vocab.forEach(this::insertTerm);
  }

  public static Vocabulary createJointVocab(List<Vocabulary> vocabs) {
    Vocabulary result = new Vocabulary();
    vocabs.forEach(v -> v.getTerms().forEach(result::insertTerm));
    return result;
  }

  public void insertTerm(Term<?> t) {
    if (hasTerm(t.getName())) {
      throw new IllegalArgumentException("Name " + t.getName() + " is already defined");
    }
    Optional<Name> dupeAlias = t.getAliases().stream().filter(this::hasTerm).findAny();
    if (dupeAlias.isPresent()) {
      throw new IllegalArgumentException("Alias " + dupeAlias.get() + " is already defined");
    }

    vocab.add(t);
  }

  public boolean isValidTerm(Term<?> t) {
    return vocab.contains(t);
  }

  public Optional<Term<?>> lookupTerm(String qName) {
    return lookupTerm(Name.ofQualified(qName));
  }

  public Optional<Term<?>> lookupTerm(Name name) {
    // TODO more efficient implementation
    return vocab.stream().filter(t -> t.getName().equals(name) || t.getAliases().stream().anyMatch(n -> n.equals(name))).findAny();
  }

  public Collection<Term<?>> getTerms() {
    return Collections.unmodifiableCollection(vocab);
  }

  public boolean hasTerm(Name name) {
    return lookupTerm(name).isPresent();
  }

  public int size() {
    return vocab.size();
  }
}
