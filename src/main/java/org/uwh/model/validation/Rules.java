package org.uwh.model.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.uwh.model.Record;
import org.uwh.model.Term;


public class Rules {
  private static final Rule ALWAYS = new Rule() {
    @Override
    public boolean isSatisfied(Record rec) {
      return true;
    }

    @Override
    public Rule withTagTranslation(Function<Integer, Integer> mapper) {
      return this;
    }
  };

  private static final Rule NEVER = new Rule() {
    @Override
    public boolean isSatisfied(Record rec) {
      return false;
    }

    @Override
    public Rule withTagTranslation(Function<Integer, Integer> mapper) {
      return this;
    }
  };

  public static Rule always() {
    return ALWAYS;
  }

  public static Rule never() {
    return NEVER;
  }

  public static Rule require(Term<?> t) {
    return new Rule() {
      @Override
      public boolean isSatisfied(Record rec) {
        return rec.get(t) != null;
      }

      @Override
      public Rule withTagTranslation(Function<Integer, Integer> mapper) {
        return require(t.withTagTranslation(mapper));
      }
    };
  }

  public static Rule all(Rule... rules) {
    return all(Arrays.asList(rules));
  }

  public static Rule all(Collection<Rule> rules) {
    return new Rule() {
      @Override
      public boolean isSatisfied(Record rec) {
        return rules.stream().allMatch(r -> r.isSatisfied(rec));
      }

      @Override
      public Rule withTagTranslation(Function<Integer, Integer> mapper) {
        List<Rule> newRules = rules.stream().map(r -> r.withTagTranslation(mapper)).toList();
        return all(newRules);
      }
    };
  }

  public static Rule requireOneOf(Term... terms) {
    return new Rule() {
      @Override
      public boolean isSatisfied(Record rec) {
        return Arrays.stream(terms).filter(t -> rec.get(t) != null).count() == 1;
      }

      @Override
      public Rule withTagTranslation(Function<Integer, Integer> mapper) {
        return requireOneOf(Arrays.stream(terms).map(t -> t.withTagTranslation(mapper)).toArray(Term[]::new));
      }
    };
  }

  public static Rule requireOneOrMoreOf(Term... terms) {
    return new Rule() {
      @Override
      public boolean isSatisfied(Record rec) {
        return Arrays.stream(terms).anyMatch(t -> rec.get(t) != null);
      }

      @Override
      public Rule withTagTranslation(Function<Integer, Integer> mapper) {
        return requireOneOrMoreOf(Arrays.stream(terms).map(t -> t.withTagTranslation(mapper)).toArray(Term[]::new));
      }
    };
  }

  public static Rule conditionally(Predicate<Record> cond, Rule ifRule, Rule elseRule) {
    return new Rule() {
      @Override
      public boolean isSatisfied(Record rec) {
        if (cond.test(rec)) {
          return ifRule.isSatisfied(rec);
        } else {
          return elseRule.isSatisfied(rec);
        }
      }

      @Override
      public Rule withTagTranslation(Function<Integer, Integer> mapper) {
        return conditionally(cond.withTagTranslation(mapper), ifRule.withTagTranslation(mapper), elseRule.withTagTranslation(mapper));
      }
    };
  }

  public static Rule conditionally(Predicate<Record> cond, Rule rule) {
    return conditionally(cond, rule, always());
  }
}
