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
  private final Set<Term> required;
  private boolean allowOthers;
  private final Set<Term> allowed;
  private final Name name;
  private final List<Rule<Record>> rules;

  public Schema(Name name) {
    this.name = name;
    this.required = new HashSet<>();
    allowOthers = true;
    allowed = new HashSet<>();
    rules = new ArrayList<>();
  }

  private Schema(Name name, Set<Term> required, Set<Term> allowed, boolean allowOthers, List<Rule<Record>> rules) {
    this.name = name;
    this.required = required;
    this.allowed = allowed;
    this.allowOthers = allowOthers;
    this.rules = rules;
  }

  public Name getName() {
    return name;
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

  public void requireConditionally(Predicate<Record> cond, Rule<Record> ifRule, Rule<Record> elseRule) {
    rules.add(Rules.conditionally(cond, ifRule, elseRule));
  }

  public void require(Rule<Record> rule) {
    rules.add(rule);
  }

  boolean isValid(Record rec) {
    Map<Term<?>,Object> values = rec.getValues();
    return required.stream().allMatch(values::containsKey)
        && rules.stream().allMatch(r -> r.isSatisfied(rec));
  }

  public boolean isValidTerm(Context ctx, Term<?> t) {
    return ctx.getVocab().isValidTerm(t) && (allowOthers || allowed.contains(t));
  }
}
