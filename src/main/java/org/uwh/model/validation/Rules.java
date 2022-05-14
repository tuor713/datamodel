package org.uwh.model.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import org.uwh.model.Record;
import org.uwh.model.Term;


public class Rules {
  private static final Rule ALWAYS = rec -> true;
  private static final Rule NEVER = rec -> false;

  public static Rule always() {
    return ALWAYS;
  }

  public static Rule never() {
    return NEVER;
  }

  public static Rule require(Term<?> t) {
    return (rec) -> rec.get(t) != null;
  }

  public static Rule all(Rule... rules) {
    return (rec) -> Arrays.stream(rules).allMatch(r -> r.isSatisfied(rec));
  }

  public static Rule all(Collection<Rule> rules) {
    return (rec) -> rules.stream().allMatch(r -> r.isSatisfied(rec));
  }

  public static Rule requireOneOf(Term... terms) {
    return rec -> Arrays.stream(terms).filter(t -> rec.get(t) != null).count() == 1;
  }

  public static Rule requireOneOrMoreOf(Term... terms) {
    return rec -> Arrays.stream(terms).anyMatch(t -> rec.get(t) != null);
  }

  public static Rule conditionally(Predicate<Record> cond, Rule ifRule, Rule elseRule) {
    return rec -> {if (cond.test(rec)) { return ifRule.isSatisfied(rec); } else { return elseRule.isSatisfied(rec); }};
  }

  public static Rule conditionally(Predicate<Record> cond, Rule rule) {
    return conditionally(cond, rule, always());
  }
}
