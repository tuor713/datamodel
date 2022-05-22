package org.uwh.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.uwh.model.types.Type;
import org.uwh.model.validation.Rule;


public class Term<T> {
  private final Name name;
  private final Set<Name> aliases;
  private final Type<T> type;
  private final List<Rule<T>> rules;

  public Term(Name name, Type<T> type, Set<Name> aliases, List<Rule<T>> rules) {
    this.name = name;
    this.type = type;
    this.aliases = new HashSet<>(aliases);
    this.rules = rules;
  }

  public static<T> Term<T> of(Name name, Type<T> type) {
    return new Term<>(name, type, Set.of(), List.of());
  }

  public static<T> Term<T> of(String qName, Type<T> type) {
    return new Term<>(Name.ofQualified(qName), type, Set.of(), List.of());
  }


  public static<T> Term<T> of(String ns, String name, Type<T> type) {
    return new Term<>(new Name(ns, name), type, Set.of(), List.of());
  }

  public Term<T> withValidation(Rule<T> rule) {
    List<Rule<T>> newRules = new ArrayList<>(rules);
    newRules.add(rule);
    return new Term<>(name, type, aliases, newRules);
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
}
