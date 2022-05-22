package org.uwh.model.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import org.uwh.model.Record;
import org.uwh.model.Term;


public class Rules {
  private static final Rule<Record> ALWAYS = rec -> true;

  private static final Rule<Record> NEVER = rec -> false;

  public static Rule<Record> always() {
    return ALWAYS;
  }

  public static Rule<Record> never() {
    return NEVER;
  }

  public static Rule<Record> require(Term<?> t) {
    return rec -> rec.get(t) != null;
  }

  public static<T> Rule<T> all(Rule<T>... rules) {
    return all(Arrays.asList(rules));
  }

  public static <T> Rule<T> all(Collection<Rule<T>> rules) {
    return rec -> rules.stream().allMatch(r -> r.isSatisfied(rec));
  }

  public static<T> Rule<T> any(Rule<T>... rules) {
    return any(Arrays.asList(rules));
  }

  public static <T> Rule<T> any(Collection<Rule<T>> rules) {
    return rec -> rules.stream().anyMatch(r -> r.isSatisfied(rec));
  }

  public static Rule<Record> requireOneOf(Term... terms) {
    return rec -> Arrays.stream(terms).filter(t -> rec.get(t) != null).count() == 1;
  }

  public static Rule<Record> requireOneOrMoreOf(Term... terms) {
    return rec -> Arrays.stream(terms).anyMatch(t -> rec.get(t) != null);
  }

  public static Rule<Record> conditionally(Predicate<Record> cond, Rule<Record> ifRule, Rule<Record> elseRule) {
    return rec -> {
      if (cond.test(rec)) {
        return ifRule.isSatisfied(rec);
      } else {
        return elseRule.isSatisfied(rec);
      }
    };
  }

  public static Rule<Record> conditionally(Predicate<Record> cond, Rule<Record> rule) {
    return conditionally(cond, rule, always());
  }

  public static Rule<Double> bounded(double min, double max, boolean minInclusive, boolean maxInclusive) {
    return rec -> (min < rec || (min == rec && minInclusive)) && (max > rec || (max == rec && maxInclusive));
  }

  public static Rule<Double> min(double min, boolean inclusive) {
    return rec -> min < rec || (min == rec && inclusive);
  }

  public static Rule<Double> max(double max, boolean inclusive) {
    return rec -> max > rec || (max == rec && inclusive);
  }

  public static Rule<Long> bounded(long min, long max, boolean minInclusive, boolean maxInclusive) {
    return rec -> (min < rec || (min == rec && minInclusive)) && (max > rec || (max == rec && maxInclusive));
  }

  public static Rule<Number> min(long min, boolean inclusive) {
    return val -> {
      long v = val.longValue();
      return min < v || (min == v && inclusive);
    };
  }

  public static Rule<Number> max(long max, boolean inclusive) {
    return val -> {
      long v = val.longValue();
      return max > v || (max == v && inclusive);
    };
  }
}
