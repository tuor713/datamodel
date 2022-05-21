package org.uwh.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.uwh.model.types.Type;
import org.uwh.model.validation.Rule;


public class Term<T> implements TagTranslation<Term<T>> {
  private final int tag;
  private final Name name;
  private final Set<Name> aliases;
  private final Type<T> type;
  private final List<Rule<T>> rules;

  public Term(int tag, Name name, Type<T> type, Set<Name> aliases, List<Rule<T>> rules) {
    this.tag = tag;
    this.name = name;
    this.type = type;
    this.aliases = new HashSet<>(aliases);
    this.rules = rules;
  }

  public static<T> Term<T> of(int tag, Name name, Type<T> type) {
    return new Term<>(tag, name, type, Set.of(), List.of());
  }

  public static<T> Term<T> of(int tag, String qName, Type<T> type) {
    return new Term<>(tag, Name.ofQualified(qName), type, Set.of(), List.of());
  }


  public static<T> Term<T> of(int tag, String ns, String name, Type<T> type) {
    return new Term<>(tag, new Name(ns, name), type, Set.of(), List.of());
  }

  public int getTag() {
    return tag;
  }

  @Override
  public Term<T> withTagTranslation(Function<Integer, Integer> mapper) {
    return new Term<>(mapper.apply(tag), name, type, aliases, rules);
  }

  public Term<T> withValidation(Rule<T> rule) {
    List<Rule<T>> newRules = new ArrayList<>(rules);
    newRules.add(rule);
    return new Term<>(tag, name, type, aliases, newRules);
  }

  public boolean isValid(T value) {
    return type.getClazz().isInstance(value) && rules.stream().allMatch(r -> r.isSatisfied(value));
  }

  public Name getName() {
    return name;
  }

  public Set<Name> getAliases() {
    return aliases;
  }

  public Type<T> getType() {
    return type;
  }

  public boolean matchesName(Name n) {
    return name.equals(n) || aliases.stream().anyMatch(n1 -> n.equals(n1));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Term term = (Term) o;
    return tag == term.tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(tag);
  }
}
