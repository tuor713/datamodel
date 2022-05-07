package org.uwh.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Schema {
  private final Vocabulary vocab;
  private final Set<Term> required;
  private boolean allowOthers;
  private final Set<Term> allowed;
  private final Name name;

  public Schema(Vocabulary vocab, Name name) {
    this.vocab = vocab;
    this.name = name;
    this.required = new HashSet<>();
    allowOthers = true;
    allowed = new HashSet<>();
  }

  public Name getName() {
    return name;
  }

  public Vocabulary getVocabulary() {
    return vocab;
  }

  public Schema require(Term<?> t) {
    required.add(t);
    allowed.add(t);
    return this;
  }

  public Schema allow(Term<?> t) {
    allowed.add(t);
    return this;
  }

  public Schema allowNoOtherTerms() {
    allowOthers = false;
    return this;
  }

  boolean isValid(Map<Integer, Object> values) {
    return required.stream().allMatch(t -> values.containsKey(t.getTag()));
  }

  public boolean isValidTerm(Term<?> t) {
    return vocab.isValidTerm(t) && (allowOthers || allowed.contains(t));
  }
}
