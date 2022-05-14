package org.uwh.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.uwh.model.validation.Rule;
import org.uwh.model.validation.Rules;


public class Schema {
  private final Vocabulary vocab;
  private final Set<Term> required;
  private boolean allowOthers;
  private final Set<Term> allowed;
  private final Name name;
  private final List<Rule> rules;

  public Schema(Vocabulary vocab, Name name) {
    this.vocab = vocab;
    this.name = name;
    this.required = new HashSet<>();
    allowOthers = true;
    allowed = new HashSet<>();
    rules = new ArrayList<>();
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

  public void requireOneOf(Term... terms) {
    rules.add(Rules.requireOneOf(terms));
  }

  public void requireOneOrMoreOf(Term... terms) {
    rules.add(Rules.requireOneOrMoreOf(terms));
  }

  public void requireConditionally(Predicate<Record> cond, Rule ifRule, Rule elseRule) {
    rules.add(Rules.conditionally(cond, ifRule, elseRule));
  }

  public void require(Rule rule) {
    rules.add(rule);
  }

  boolean isValid(Record rec) {
    Map<Integer,Object> values = rec.getValues();
    return required.stream().allMatch(t -> values.containsKey(t.getTag()))
        && rules.stream().allMatch(r -> r.isSatisfied(rec));
  }

  public boolean isValidTerm(Term<?> t) {
    return vocab.isValidTerm(t) && (allowOthers || allowed.contains(t));
  }
}
