package org.uwh.model.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.uwh.model.Record;
import org.uwh.model.Term;


public class Rules {
  private static final Rule<Record> ALWAYS = new Rule<>() {
    @Override
    public boolean isSatisfied(Record rec) {
      return true;
    }

    @Override
    public Rule<Record> withTagTranslation(Function<Integer, Integer> mapper) {
      return this;
    }
  };

  private static final Rule<Record> NEVER = new Rule<>() {
    @Override
    public boolean isSatisfied(Record rec) {
      return false;
    }

    @Override
    public Rule<Record> withTagTranslation(Function<Integer, Integer> mapper) {
      return this;
    }
  };

  public static Rule<Record> always() {
    return ALWAYS;
  }

  public static Rule<Record> never() {
    return NEVER;
  }

  public static Rule<Record> require(Term<?> t) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(Record rec) {
        return rec.get(t) != null;
      }

      @Override
      public Rule<Record> withTagTranslation(Function<Integer, Integer> mapper) {
        return require(t.withTagTranslation(mapper));
      }
    };
  }

  public static<T> Rule<T> all(Rule<T>... rules) {
    return all(Arrays.asList(rules));
  }

  public static <T> Rule<T> all(Collection<Rule<T>> rules) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(T rec) {
        return rules.stream().allMatch(r -> r.isSatisfied(rec));
      }

      @Override
      public Rule<T> withTagTranslation(Function<Integer, Integer> mapper) {
        List<Rule<T>> newRules = rules.stream().map(r -> r.withTagTranslation(mapper)).toList();
        return all(newRules);
      }
    };
  }

  public static<T> Rule<T> any(Rule<T>... rules) {
    return any(Arrays.asList(rules));
  }

  public static <T> Rule<T> any(Collection<Rule<T>> rules) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(T rec) {
        return rules.stream().anyMatch(r -> r.isSatisfied(rec));
      }

      @Override
      public Rule<T> withTagTranslation(Function<Integer, Integer> mapper) {
        List<Rule<T>> newRules = rules.stream().map(r -> r.withTagTranslation(mapper)).toList();
        return all(newRules);
      }
    };
  }

  public static Rule<Record> requireOneOf(Term... terms) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(Record rec) {
        return Arrays.stream(terms).filter(t -> rec.get(t) != null).count() == 1;
      }

      @Override
      public Rule<Record> withTagTranslation(Function<Integer, Integer> mapper) {
        return requireOneOf(Arrays.stream(terms).map(t -> t.withTagTranslation(mapper)).toArray(Term[]::new));
      }
    };
  }

  public static Rule<Record> requireOneOrMoreOf(Term... terms) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(Record rec) {
        return Arrays.stream(terms).anyMatch(t -> rec.get(t) != null);
      }

      @Override
      public Rule<Record> withTagTranslation(Function<Integer, Integer> mapper) {
        return requireOneOrMoreOf(Arrays.stream(terms).map(t -> t.withTagTranslation(mapper)).toArray(Term[]::new));
      }
    };
  }

  public static Rule<Record> conditionally(Predicate<Record> cond, Rule<Record> ifRule, Rule<Record> elseRule) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(Record rec) {
        if (cond.test(rec)) {
          return ifRule.isSatisfied(rec);
        } else {
          return elseRule.isSatisfied(rec);
        }
      }

      @Override
      public Rule<Record> withTagTranslation(Function<Integer, Integer> mapper) {
        return conditionally(cond.withTagTranslation(mapper), ifRule.withTagTranslation(mapper), elseRule.withTagTranslation(mapper));
      }
    };
  }

  public static Rule<Record> conditionally(Predicate<Record> cond, Rule<Record> rule) {
    return conditionally(cond, rule, always());
  }

  public static Rule<Double> bounded(double min, double max, boolean minInclusive, boolean maxInclusive) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(Double rec) {
        return (min < rec || (min == rec && minInclusive)) && (max > rec || (max == rec && maxInclusive));
      }

      @Override
      public Rule<Double> withTagTranslation(Function<Integer, Integer> mapper) {
        return this;
      }
    };
  }

  public static Rule<Double> min(double min, boolean inclusive) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(Double rec) {
        return min < rec || (min == rec && inclusive);
      }

      @Override
      public Rule<Double> withTagTranslation(Function<Integer, Integer> mapper) {
        return this;
      }
    };
  }

  public static Rule<Double> max(double max, boolean inclusive) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(Double rec) {
        return max > rec || (max == rec && inclusive);
      }

      @Override
      public Rule<Double> withTagTranslation(Function<Integer, Integer> mapper) {
        return this;
      }
    };
  }

  public static Rule<Long> bounded(long min, long max, boolean minInclusive, boolean maxInclusive) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(Long rec) {
        return (min < rec || (min == rec && minInclusive)) && (max > rec || (max == rec && maxInclusive));
      }

      @Override
      public Rule<Long> withTagTranslation(Function<Integer, Integer> mapper) {
        return this;
      }
    };
  }

  public static Rule<Number> min(long min, boolean inclusive) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(Number val) {
        long v = val.longValue();
        return min < v || (min == v && inclusive);
      }

      @Override
      public Rule<Number> withTagTranslation(Function<Integer, Integer> mapper) {
        return this;
      }
    };
  }

  public static Rule<Number> max(long max, boolean inclusive) {
    return new Rule<>() {
      @Override
      public boolean isSatisfied(Number val) {
        long v = val.longValue();
        return max > v || (max == v && inclusive);
      }

      @Override
      public Rule<Number> withTagTranslation(Function<Integer, Integer> mapper) {
        return this;
      }
    };
  }
}
